/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.uxsoft.net.io.common.IoListener;
import com.uxsoft.net.io.common.IoSession;
import com.uxsoft.net.io.common.TrafficMask;

/**
 *
 * @author David
 */
public class BlockingConnector {
	private Socket socket;
	private InputStream input;
	private OutputStream output;
	private boolean connected = false;
	private BlockingProtocol protocol;
	private IoSession session;
	private IoListener listener;
	private InputProcessor inputProcessor;
	private OutputProcessor outputProcessor;
	private LinkedList<BlockingWriteRequest> writeQueue = new LinkedList();
	private ReentrantReadWriteLock rwL = new ReentrantReadWriteLock();
	private Lock rLock = rwL.readLock();
	private Lock wLock = rwL.writeLock();
	private Executor executor;
	private Lock inputProcessLock = new ReentrantLock(), outputProcessLock = new ReentrantLock();

	public BlockingConnector(Executor executor) {
		if (executor == null) {
			throw new NullPointerException("Executor Can't Be NULL!~");
		}
		this.executor = executor;
	}

	public void connect(InetSocketAddress endPoint, IoListener listener, BlockingProtocol protocol)
			throws IOException {
		socket = new Socket(endPoint.getAddress(), endPoint.getPort());
		input = socket.getInputStream();
		output = socket.getOutputStream();
		connected = socket.isConnected();
		this.listener = listener;
		this.protocol = protocol;
		session = new IoSession() {

			Map<String, Object> attrs = new HashMap();

			public void write(Object message) {
				BlockingConnector.this.write(message);
			}

			public SelectionKey getSelectionKey() {
				return null;
			}

			public SocketChannel getSocketChannel() {
				return null;
			}

			public int getSentBytes() {
				return 0;
			}

			public int getReceivedBytes() {
				return 0;
			}

			public int getTotalBytes() {
				return 0;
			}

			public TrafficMask getTrafficMask() {
				return null;
			}

			public Object removeAttribute(String key) {
				return attrs.remove(key);
			}

			public boolean hasAttribute(String key) {
				return attrs.containsKey(key);
			}

			public void setAttribute(String key, Object o) {
				attrs.put(key, o);
			}

			public Object getAttribute(String key) {
				return attrs.get(key);
			}

			public SocketAddress getRemoteAddress() {
				return socket.getRemoteSocketAddress();
			}

			public void close() {
				BlockingConnector.this.close();
			}

			public boolean isConnected() {
				return connected();
			}
		};

		inputProcessor = new InputProcessor();
		outputProcessor = new OutputProcessor();
		executor.execute(inputProcessor);
		executor.execute(outputProcessor);
	}

	public IoSession getSession() {
		return session;
	}

	public boolean connected() {
		return connected;
	}

	public void close() {
		inputProcessLock.lock();
		outputProcessLock.lock();
		try {
			try {
				input.close();
				output.close();
				socket.close();
				socket = null;
			} catch (Exception e) {
				e.printStackTrace();
			}  finally {
				socket = null;
				input = null;
				output = null;
				connected = false;
			}
		} finally {
			inputProcessLock.unlock();
			outputProcessLock.unlock();
		}
	}

	public void write(Object obj) {
		try {

			byte[] bytes = protocol.encodeObject(session, obj, null); // todo
			wLock.lock();
			try {
				writeQueue.addLast(new MyWriteRequest(bytes, obj));
			} finally {
				wLock.unlock();
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	class InputProcessor
			implements Runnable {

		public void run() {
			byte[] buf = new byte[2048];
			while (connected) {
				try {
					int read = 0;
					inputProcessLock.lock();
					try {
						read = input.read(buf);
					} finally {
						inputProcessLock.unlock();
					}
					if (read <= 0) {
						listener.onSessionClosed(session);
						return;
					}
					DecoderOutputImpl decoder = new DecoderOutputImpl();
					protocol.decodeObject(session, buf, read, decoder);
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
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

	}

	class OutputProcessor
			implements Runnable {

		private LinkedList<BlockingWriteRequest> byteQueue = new LinkedList();

		public void run() {
			while (connected) {
				try {
					rLock.lock();
					try {
						if (writeQueue.size() > 0) {
							wLock.lock();
							try {
								while (!writeQueue.isEmpty()) {
									BlockingWriteRequest lastWriteReq = writeQueue.pollFirst();
									byteQueue.addLast(lastWriteReq);
								}
							} finally {
								wLock.unlock();
							}
						}
					} finally {
						rLock.unlock();
					}
					outputProcessLock.lock();
					try {
						while (!byteQueue.isEmpty()) {
							BlockingWriteRequest req = writeQueue.poll();
							listener.onMessageSent(session, req.getMessage());
							output.write(req.getBytes());
							req.dispose();
						}
					} finally {
						outputProcessLock.unlock();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	static class DecoderOutputImpl
			implements BlockingDecoderOutput {
		List<Object> oList;

		public void write(Object out) {
			if (oList == null) {
				oList = new LinkedList();
			}
			oList.add(out);
		}
	}

	static class MyWriteRequest implements BlockingWriteRequest {
		byte[] data;
		Object message;

		public MyWriteRequest(byte[] data, Object message) {
			this.data = data;
			this.message = message;
		}

		public byte[] getBytes() {
			return data;
		}

		public Object getMessage() {
			return message;
		}

		public void dispose() {
			data = null;
			message = null;
		}
	}
}
