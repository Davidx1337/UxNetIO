/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
import com.uxsoft.net.io.common.TrafficMask;
import com.uxsoft.net.io.protocol.DecoderOutput;
import com.uxsoft.net.io.protocol.EncoderOutput;
import com.uxsoft.net.io.protocol.Protocol;
import com.uxsoft.net.io.util.ExceptionMonitor;
import com.uxsoft.net.io.util.NewThreadExecutor;

/**
 *
 * @author David
 */
public class SocketConnector {
	private static final int WriteSpinCount = 256;
	private final Object lock = new Object();
	private boolean connected = false;
	private SocketChannel channel;
	private SelectionKey key;
	private volatile Selector selector;
	private Protocol protocol;
	private IoListener listener;
	private IoWorker worker;
	private SocketIoSession session;
	private Queue<TrafficChange> trafficChangeQueue = new ConcurrentLinkedQueue();
	private Executor executor;

	public SocketConnector() {
		this(new NewThreadExecutor());
	}

	public SocketConnector(Executor executor) {
		if (executor == null) {
			throw new IllegalArgumentException("EXECUTOR");
		}
		this.executor = executor;
	}

	public void connect(InetSocketAddress remote, Protocol protocol, IoListener listener)
			throws IOException {
		connect(remote, protocol, listener, null);
	}

	public void connect(InetSocketAddress remote, Protocol protocol, IoListener listener, SocketConfiguration configuration)
			throws IOException {
		if (channel != null) {
			throw new RuntimeException("Already connected");
		}
		if (protocol == null || listener == null || remote == null) {
			throw new IllegalArgumentException();
		}
		//System.err.println("connecting");
		channel = SocketChannel.open();
		//System.err.println("openchannel");
		if (configuration != null) {
			Socket sock = channel.socket();
			sock.setKeepAlive(configuration.isKeepAlive());
			sock.setOOBInline(configuration.isOOBInline());
			sock.setReceiveBufferSize(configuration.getReceiveBufferSize());
			sock.setReuseAddress(configuration.isReuseAddress());
			sock.setSendBufferSize(configuration.getSendBufferSize());
			sock.setSoLinger(configuration.isLingerOn(), configuration.getLinger());
			sock.setSoTimeout(configuration.getTimeout());
			sock.setTcpNoDelay(configuration.isTcpNoDelay());
		}
		//System.err.println("reuseaddress");
		channel.configureBlocking(false);
		//System.err.println("configureblocking");
		channel.connect(remote);
		//System.err.println("connect");
		/*if (!connected) {
			while (!channel.finishConnect()) {
				try {
					Thread.sleep(1);
				} catch (Throwable t) {}
			}
		}*/
		selector = SelectorProvider.provider().openSelector();
		//System.err.println("openselector");
		//key = channel.register(selector, SelectionKey.OP_CONNECT, key);
		//changeRequestQueue.add(new ChangeRequest(ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
		worker = new IoWorker();
		//System.err.println("startworker");
		this.protocol = protocol;
		this.listener = listener;
		//System.err.println("setprotocol/listener");
		session = new SocketIoSession(channel, null) {
			@Override
			public void close() {
				try {
					SocketConnector.this.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void write(Object message) {
				SocketConnector.this.write(message);
			}

			@Override
			public boolean isConnected() {
				return (channel != null && channel.isConnected() && connected);
			}

			@Override
			public void updateTrafficMask() {
				trafficChangeQueue.add(new TrafficChange(getTrafficMask().getInterestOps()));
			}
		};
		//System.err.println("createsession");
		try {
			listener.onSessionCreated(session);
		} catch (Exception e) {
			fireExceptionOccurred(session, e);
		}
		key = channel.register(selector, SelectionKey.OP_CONNECT, session);
		executor.execute(worker);
	}

	/**
	 * Waits indefinitely until this client is connected
	 */
	public void waitUntilConnected() {
		waitUntilConnected(0);
	}

	/**
	 * Waits timeout milliseconds for this client to be connected
	 * @param timeout
	 */
	public void waitUntilConnected(long timeout) {
		long start = System.currentTimeMillis();
		while (!connected && (System.currentTimeMillis() - start < timeout) || timeout <= 0) {
			synchronized (lock) {
				if (connected) {
					return;
				}
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
	}

	private void fireExceptionOccurred(IoSession session, Throwable t) {
		if (session == null) {
			ExceptionMonitor.Monitor.error("Global Exception", t);
		} else {
			try {
				listener.onExceptionOccurred(session, t);
			} catch (Exception e) {
				ExceptionMonitor.Monitor.error("Error in ExceptionOccurred Handler", e);
			}
		}
	}

	private void finishConnection()
			throws IOException {
		//boolean x = true;
		/*while (x) {
			System.err.println("FinishCon");
		}*/
		//System.err.println("register");
		synchronized (lock) {
			// Finish the connection. If the connection operation failed
			// this will raise an IOException.
			try {
				channel.finishConnect();
			} catch (IOException e) {
				e.printStackTrace();
				// Cancel the channel's registration with our selector
				key.cancel();
				return;
			}
			try {
				listener.onSessionOpened(session);
			} catch (Exception e) {
				fireExceptionOccurred(session, e);
			}
			//System.err.println("sessionopened");
			session.setKey(key);
			//System.err.println("finishconnection");
			key.interestOps(SelectionKey.OP_READ);
			connected = true;
		}
	}

	public void close()
			throws IOException {
		if ((channel.isConnected() || selector.isOpen()) && session != null){
			synchronized (lock) {
				key.cancel();
				channel.close();
				selector.close();
				try {
					listener.onSessionClosed(session);
				} catch (Exception e) {
					fireExceptionOccurred(session, e);
				}
				session = null;
				connected = false;
				channel = null;
			}
		}
	}

	public void write(Object obj) {
		EncoderOutputImpl output = new EncoderOutputImpl();
		protocol.getEncoder().encode(session, obj, output);
		if (output.bb != null) {
			session.getWriteBufferQueue().add(new SocketWriteRequest(obj, output.bb));
			if (!session.getTrafficMask().canWrite()) {
				interestOps(session.getTrafficMask().getInterestOps() | SelectionKey.OP_WRITE);
				selector.wakeup();
				//System.err.println("InterestOpping write");
			}
		}
	}

	private void interestOps(int newOps) {
		trafficChangeQueue.add(new TrafficChange(newOps));
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public SocketIoSession getSession() {
		return session;
	}

	/*private void processChanges()
			throws IOException {
		while (true) {
			ChangeRequest req = changeRequestQueue.poll();
			if (req == null) {
				break;
			}
			switch (req.type) {
				case ChangeRequest.CHANGE_OP:
					key.interestOps(req.val);
					break;
				default:
					throw new RuntimeException("Unknown change: " + req.type + " [" + req.val + "]");
			}
		}
	}*/

	private void process(Set<SelectionKey> keys)
			throws IOException {
		Iterator<SelectionKey> keyItr = keys.iterator();
		while (keyItr.hasNext()) {
			SelectionKey key = keyItr.next();
			keyItr.remove();
			if (!key.isValid()) {
				continue;
			}

			if (key.isReadable()) {
				read();
				////System.err.println("READ");
			} else if (key.isWritable()) {
				doFlush();
				////System.err.println("WRITE");
			} else if (key.isConnectable()) {
				finishConnection();
			}
		}
	}

	private void read()
			throws IOException {
		//System.err.println("Reading.");
		if (!channel.isConnected()) {
			return;
		}
		int readBytes = 0;
		int read;
		ByteBuffer buf = ByteBuffer.allocate(session.getReadBufferSize());
		try {
			while ((read = channel.read(buf.buf())) > 0) {
				readBytes += read;
			}
		} catch (IOException e) {
			fireExceptionOccurred(session, e);
			close();
			return;
		} finally {
			buf.flip();
		}
		if (readBytes > 0) {
			try {
				session.increaseReceivedBytes(readBytes);
				////System.err.println("Data recieved: " + readBytes + " bytes.");
				DecoderOutputImpl decoder = new DecoderOutputImpl();
				protocol.getDecoder().decode(session, buf, decoder);
				List<Object> messages = decoder.oList;
				if (messages != null) {
					Iterator<Object> messageIterator = messages.iterator();
					while (messageIterator.hasNext()) {
						Object msg = messageIterator.next();
						messageIterator.remove();
						if (msg != null) {
							listener.onMessageReceived(session, msg);
							////System.err.println("onMessageReceived() Invoked");
						}
					}
				}
				decoder = null;
				buf = null;
			} catch (Throwable t) {
				fireExceptionOccurred(session, t);
			}
		}
		if (read < 0) {
			close();
		}
		//System.err.println("Read End");
	}

	private boolean doFlush()
			throws IOException {
		SocketChannel chan = session.getSocketChannel();
		Queue<SocketWriteRequest> writeBufferQueue = session.getWriteBufferQueue();
		if (!chan.isConnected() || writeBufferQueue.size() == 0) {
			return false;
		}

		//System.err.println("doFlush");
		// Clear OP_WRITE
		key.interestOps(SelectionKey.OP_READ);
		session.setTrafficMask(TrafficMask.READ);
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
				////System.err.println("Writing: " + buf.toString() + " [" + req.getMessage() + "]");
				if (buf.remaining() == 0) { // Done write
					writeBufferQueue.poll(); // Remove request
					try {
						listener.onMessageSent(session, req.getMessage()); // Notify listener
					} catch (Exception e) {
						fireExceptionOccurred(session, e);
					}
					////System.err.println("onMessageSent() Invoked");
					continue;
				}

				int localWrittenBytes = 0;
				try {
					for (int i = WriteSpinCount; i > 0; i --) {
						localWrittenBytes = chan.write(buf.buf());
						if (localWrittenBytes != 0 || !buf.hasRemaining()) {
							break;
						}
					}
				} catch (Throwable t) {
					fireExceptionOccurred(session, t);
					close();
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

		//System.err.println("endDoFlush");

		return true;
	}

	private class TrafficChange {
		private int newOps;

		public TrafficChange(int newOps) {
			this.newOps = newOps;
		}
	}

	class IoWorker
			implements Runnable {
		public void run() {
			Selector selector = SocketConnector.this.selector;
			while (selector.isOpen()) {
				if (trafficChangeQueue.size() > 0) {
					while (true) {
						TrafficChange change = trafficChangeQueue.poll();
						if (change == null) {
							break;
						}
						key.interestOps(change.newOps);
						session.setTrafficMask(TrafficMask.getInstance(change.newOps));
					}
				}
				//System.err.println("Select Start");
				try {
					//processChanges();
					int nKeys = selector.select(1000);
					//System.err.println(nKeys + " events");
					if (nKeys > 0) {
						process(selector.selectedKeys());
					}
				} catch (Throwable e) {
					fireExceptionOccurred(session, e);
				}
				//System.err.println("Select End");
			}
			//System.err.println("IoWorker END");
		}
	}

	static class EncoderOutputImpl
			implements EncoderOutput {
		ByteBuffer bb;

		public void write(ByteBuffer out) {
			if (bb != null) {
				throw new IllegalStateException();
			}
			bb = out;
		}
	}

	static class DecoderOutputImpl
			implements DecoderOutput {
		List<Object> oList;

		public void write(Object out) {
			if (oList == null) {
				oList = new LinkedList();
			}
			oList.add(out);
		}
	}

	static class ChangeRequest {
		public static final int
				CHANGE_OP = 0x01
		;
		int type;
		int val;

		public ChangeRequest(int type, int val) {
			this.type = type;
			this.val = val;
		}
	}
}
