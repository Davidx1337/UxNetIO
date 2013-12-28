/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import com.uxsoft.net.io.common.ByteBuffer;
import com.uxsoft.net.io.common.IoListener;
import com.uxsoft.net.io.common.IoSession;
import com.uxsoft.net.io.protocol.DecoderOutput;
import com.uxsoft.net.io.protocol.EncoderOutput;
import com.uxsoft.net.io.protocol.Protocol;
import com.uxsoft.net.io.util.ExceptionMonitor;
import com.uxsoft.net.io.util.NewThreadExecutor;

/**
 *
 * @author David
 */
public class SocketAcceptor {

    private int port;
    private ServerSocketChannel sChan;
    private SelectionKey serverKey;
    private Selector selector;
    private Protocol protocol;
    private IoProcessor[] processors;
    private InetAddress hostAddress;
    private IoListener listener;
    private static final int WriteSpinCount = 256;
    private IoWorker worker;
    private int processorDistributor = 0;
    private Executor executor;
    private SocketConfiguration config = null;

    public SocketAcceptor(int port) {
        this(port, new NewThreadExecutor());
    }

    public SocketAcceptor(int port, int processors) {
        this(port, processors, new NewThreadExecutor());
    }

    public SocketAcceptor(int port, Executor executor) {
        this(port, Runtime.getRuntime().availableProcessors(), executor);
    }

    public SocketAcceptor(int port, int processorCount, Executor executor) {
        this.port = port;
        processors = new IoProcessor[processorCount];
        for (int i = 0; i < processorCount; i++) {
            processors[i] = new IoProcessor();
        }
        this.executor = executor;
    }

    public void bind(Protocol protocol, IoListener listener) {
        bind(protocol, listener, null);
    }

    public void bind(Protocol protocol, IoListener listener, SocketConfiguration configuration) {
        if (sChan != null) {
            throw new IllegalStateException("Already bound");
        }
        if (protocol == null) {
            throw new IllegalArgumentException("PROTOCOL");
        }
        if (listener == null) {
            throw new IllegalArgumentException("LISTENER");
        }
        this.protocol = protocol;
        this.listener = listener;
        try {
            sChan = ServerSocketChannel.open();
            sChan.configureBlocking(false);
            if (configuration != null) {
                /*ServerSocket sock = sChan.socket();
                 sock.setReceiveBufferSize(configuration.getReceiveBufferSize());
                 sock.setReuseAddress(configuration.isReuseAddress());
                 sock.setSoTimeout(configuration.getTimeout());*/
                config = configuration;
            }
            sChan.socket().bind(new InetSocketAddress(hostAddress, port));
            selector = SelectorProvider.provider().openSelector();
            this.serverKey = sChan.register(selector, SelectionKey.OP_ACCEPT, this);
            worker = new IoWorker();
            executor.execute(worker);
            for (int i = 0; i < processors.length; i++) {
                IoProcessor processor = processors[i];
                processor.startup();
                executor.execute(processors[i]);
            }
        } catch (IOException e) {
            e.printStackTrace(); // for now
        } finally {
            selector.wakeup();
        }
    }

    public void unbind()
            throws IOException {
        if (sChan == null) {
            throw new IllegalStateException();
        }
        sChan.close();
        selector.close();
        serverKey.cancel();
        for (IoProcessor processor : processors) {
            processor.selector.close();
        }
    }

    IoProcessor nextProcessor() {
        if (processorDistributor + 1 == Integer.MAX_VALUE) {
            processorDistributor = 0;
        }
        return processors[(processorDistributor++) % processors.length];
    }

    public void setHostAddress(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (sChan != null) {
            throw new IllegalStateException("Already bound");
        }
        this.port = port;
    }

    public InetAddress getHostAddress() {
        return hostAddress;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    private final void processKeys(final Set<SelectionKey> keys) {
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            if (!key.isValid()) {
                continue; // Invalid key
            }

            try {
                if (key.isAcceptable()) {
                    accept(key);
                }
            } catch (IOException e) {
                e.printStackTrace(); // for now
            }
        }
    }

    private SocketIoSession accept(SelectionKey key)
            throws IOException {
        ServerSocketChannel schan = (ServerSocketChannel) key.channel();
        SocketChannel chan = schan.accept();
        chan.configureBlocking(false);
        if (config != null) {
            Socket sock = chan.socket();
            sock.setKeepAlive(config.isKeepAlive());
            sock.setOOBInline(config.isOOBInline());
            sock.setReceiveBufferSize(config.getReceiveBufferSize());
            sock.setReuseAddress(config.isReuseAddress());
            sock.setSendBufferSize(config.getSendBufferSize());
            sock.setSoLinger(config.isLingerOn(), config.getLinger());
            sock.setSoTimeout(config.getTimeout());
            sock.setTcpNoDelay(config.isTcpNoDelay());
        }
        IoProcessor processor = nextProcessor();
        SocketIoSession session = new SocketIoSession(chan, processor);
        processor.registerSession(session);
        try {
            listener.onSessionCreated(session);
        } catch (Exception e) {
            fireExceptionOccurred(session, e);
        }

        return session;
    }

    class IoWorker
            implements Runnable {

        @Override
        public void run() {
            final Selector selector = SocketAcceptor.this.selector;
            while (selector.isOpen()) {
                try {
                    int nKeys = selector.select();
                    if (nKeys > 0) {
                        processKeys(selector.selectedKeys());
                    }
                } catch (Throwable t) {
                    t.printStackTrace(); // For now
                }
            }
        }
    }

    private void fireExceptionOccurred(IoSession session, Throwable t) {
        if (session == null) {
            ExceptionMonitor.Monitor.error("Global Exception", t);
        } else {
            try {
                listener.onExceptionOccurred(session, t);
            } catch (Throwable e) {
                ExceptionMonitor.Monitor.error("Error in ExceptionOccurred Handler", e);
            }
        }
    }

    class IoProcessor
            implements Runnable {

        private Queue<SocketIoSession> flushingSessions = new ConcurrentLinkedQueue();
        private Queue<SocketIoSession> removingSessions = new ConcurrentLinkedQueue();
        private Queue<SocketIoSession> registerQueue = new ConcurrentLinkedQueue();
        private Queue<SocketIoSession> trafficUpdateQueue = new ConcurrentLinkedQueue();
        volatile Selector selector;
        final Object lock = new Object();

        public ByteBuffer encodeObject(IoSession session, Object o) {
            EncoderOutputImpl out = new EncoderOutputImpl();
            protocol.getEncoder().encode(session, o, out);
            return out.bb;
        }

        public void updateTrafficMask(SocketIoSession session) {
            if (!session.isTrafficUpdateRequested()) {
                scheduleTrafficUpdate(session);
            }
            wakeupSelector();
        }

        public void scheduleTrafficUpdate(SocketIoSession session) {
            if (session.isTrafficUpdateRequested()) {
                return;
            }
            session.setTrafficUpdateRequested(true);
            trafficUpdateQueue.add(session);
        }

        public void registerSession(SocketIoSession session) {
            registerQueue.add(session);
            wakeupSelector();
        }

        private void startup()
                throws IOException {
            selector = SelectorProvider.provider().openSelector();
        }

        private void wakeupSelector() {
            synchronized (lock) {
                if (selector != null) {
                    selector.wakeup();
                }
            }
        }

        private void processSelection(Set<SelectionKey> keys)
                throws IOException {
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (!key.isValid()) {
                    continue;
                }
                SocketIoSession session = (SocketIoSession) key.attachment();
                if (key.isReadable()) {
                    read(session);
                } else if (key.isWritable()) {
                    scheduleFlush(session);
                }
            }
        }

        @Override
        public void run() {
            final Selector selector = this.selector;
            while (selector.isOpen()) {
                try {
                    int nKeys = selector.select(1000);
                    doRegisterNew();
                    doUpdateTrafficMasks();
                    if (nKeys > 0) {
                        processSelection(selector.selectedKeys());
                    }
                    doFlushAll();
                    doRemove();
                } catch (Throwable t) {
                    t.printStackTrace(); // for now
                }
            }
        }

        private void read(SocketIoSession session)
                throws IOException {
            ByteBuffer buf = ByteBuffer.allocate(session.getReadBufferSize());
            SocketChannel channel = session.getSocketChannel();
            int readBytes = 0;
            int read;
            try {
                while ((read = channel.read(buf.buf())) > 0) {
                    readBytes += read;
                }
            } catch (Exception e) {
                fireExceptionOccurred(session, e);
                scheduleRemove(session);
                return;
            } finally {
                buf.flip();
            }
            if (readBytes > 0) {
                session.increaseReceivedBytes(readBytes);
                //System.err.println("Data recieved: " + readBytes + " bytes.");
                DecoderOutputImpl decoder = new DecoderOutputImpl();
                protocol.getDecoder().decode(session, buf, decoder);
                List<Object> oList = decoder.oList;
                if (oList != null) {
                    Iterator<Object> oItr = oList.iterator();
                    while (oItr.hasNext()) {
                        Object msg = oItr.next();
                        oItr.remove();
                        if (msg != null) {
                            try {
                                listener.onMessageReceived(session, msg);
                            } catch (Exception e) {
                                fireExceptionOccurred(session, e);
                            }
                            //System.err.println("onMessageReceived() Invoked");
                        }
                    }
                }
                buf = null;
            }
            if (read < 0) {
                scheduleRemove(session);
            }
        }

        class DecoderOutputImpl
                implements DecoderOutput {

            List<Object> oList;

            public void write(Object out) {
                if (oList == null) {
                    oList = new LinkedList();
                }
                oList.add(out);
            }
        }

        private void doFlushAll() {
            if (flushingSessions.isEmpty()) {
                return;
            }
            while (true) {
                SocketIoSession flush = flushingSessions.poll();
                if (flush == null) {
                    break;
                }
                flush.setFlushQueued(false);
                try {
                    boolean flushedOk = doFlush(flush);
                    if (flushedOk && !flush.getWriteBufferQueue().isEmpty() && !flush.isFlushQueued()) {
                        scheduleFlush(flush);
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // For now
                    scheduleRemove(flush);
                }
            }
        }

        public class EncoderOutputImpl
                implements EncoderOutput {

            ByteBuffer bb;

            public void write(ByteBuffer out) {
                if (bb != null) {
                    throw new IllegalStateException();
                }
                bb = out;
            }
        }

        private boolean doFlush(SocketIoSession session)
                throws IOException {
            SocketChannel chan = session.getSocketChannel();
            if (!chan.isConnected()) {
                return false;
            }

            SelectionKey key = session.getSelectionKey();
            int newOps = key.interestOps() & (~SelectionKey.OP_WRITE);
            key.interestOps(newOps);
            //session.setTrafficMask(TrafficMask.getInstance(newOps));
            Queue<SocketWriteRequest> writeBufferQueue = session.getWriteBufferQueue();
            int writtenBytes = 0;
            int maxWrittenBytes = session.getWriteBufferSize() << 1;
            try {
                while (true) {
                    SocketWriteRequest req = writeBufferQueue.peek();
                    if (req == null) {
                        break;
                    }
                    ByteBuffer buf = req.getBuffer();
                    if (buf == null) {
                        throw new IllegalStateException("Null buffer");
                    }
                    //System.err.println("Writing: " + buf.toString() + " [" + req.getMessage() + "]");
                    if (buf.remaining() == 0) { // Done write
                        writeBufferQueue.poll(); // Remove request
                        try {
                            listener.onMessageSent(session, req.getMessage()); // Notify listener
                        } catch (Exception e) {
                            fireExceptionOccurred(session, e);
                        }
                        //System.err.println("onMessageSent() Invoked");
                        continue;
                    }

                    int localWrittenBytes = 0;
                    for (int i = WriteSpinCount; i > 0; i--) {
                        localWrittenBytes = chan.write(buf.buf());
                        if (localWrittenBytes != 0 || !buf.hasRemaining()) {
                            break;
                        }
                    }

                    writtenBytes += localWrittenBytes;

                    if (localWrittenBytes == 0 || writtenBytes >= maxWrittenBytes) {
                        // Kernel buffer is full or wrote too much.
                        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                        return false;
                    }
                }
            } finally {
                session.increaseSentBytes(writtenBytes);
            }

            return true;
        }

        private void doRemove() {
            while (true) {
                SocketIoSession removed = removingSessions.poll();
                if (removed == null) {
                    break;
                }

                SelectionKey key = removed.getSelectionKey();

                if (key == null) {
                    scheduleRemove(removed);
                    continue;
                }

                if (!key.isValid()) {
                    continue; // Invalid key
                }

                try {
                    //System.err.println("Connection from: " + removed.getSocketChannel().socket().getRemoteSocketAddress() + " closed.");
                    key.cancel();
                    removed.getSocketChannel().close();
                } catch (Throwable t) {
                    fireExceptionOccurred(removed, t);
                } finally {
                    try {
                        listener.onSessionClosed(removed);
                    } catch (Exception e) {
                        fireExceptionOccurred(removed, e);
                    }
                }
            }
        }

        private void doRegisterNew() {
            if (registerQueue.size() == 0) {
                return;
            }
            final Selector selector = this.selector;
            while (true) {
                SocketIoSession register = registerQueue.poll();
                if (register == null) {
                    break;
                }
                try {
                    SocketChannel schan = register.getSocketChannel();
                    schan.configureBlocking(false);
                    register.setKey(schan.register(selector, SelectionKey.OP_READ, register));
                    //System.err.println("Connection accepted from: " + schan.socket().getRemoteSocketAddress());
                    listener.onSessionOpened(register);
                } catch (Throwable t) {
                    fireExceptionOccurred(register, t);
                    scheduleRemove(register);
                }
            }
        }

        public boolean scheduleFlush(SocketIoSession session) {
            if (!session.isFlushQueued()) {
                flushingSessions.add(session);
                session.setFlushQueued(true);
                return true;
            }
            return false;
        }

        public void flushSession(SocketIoSession session) {
            if (scheduleFlush(session)) {
                wakeupSelector();
            }
        }

        public void scheduleRemove(SocketIoSession session) {
            removingSessions.add(session);
        }

        private void doUpdateTrafficMasks() {
            if (trafficUpdateQueue.isEmpty()) {
                return;
            }

            while (true) {
                SocketIoSession session = trafficUpdateQueue.poll();

                if (session == null) {
                    break;
                }

                session.setTrafficUpdateRequested(false);
                SelectionKey key = session.getSelectionKey();

                if (key == null) {
                    scheduleTrafficUpdate(session);
                    break;
                }

                if (!key.isValid()) {
                    continue;
                }

				// The normal is OP_READ and, if there are write requests in the
                // session's write queue, set OP_WRITE to trigger flushing.
                int ops = SelectionKey.OP_READ;
                Queue<SocketWriteRequest> writeRequestQueue = session.getWriteBufferQueue();
                //System.err.println("Updating traffic mask..");
                synchronized (writeRequestQueue) {
                    if (!writeRequestQueue.isEmpty()) {
                        ops |= SelectionKey.OP_WRITE;
                        //System.err.println("Write enabled");
                    }
                }

				// Now mask the preferred ops with the mask of the current session
                //int mask = session.getTrafficMask().getInterestOps();
                key.interestOps(ops/* & mask*/);
            }
        }
    }
}
