/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import com.uxsoft.net.io.common.ByteBuffer;
import com.uxsoft.net.io.common.IoListener;
import com.uxsoft.net.io.protocol.EncoderOutput;
import com.uxsoft.net.io.protocol.Protocol;
import com.uxsoft.net.io.util.NewThreadExecutor;

/**
 *
 * @author David
 */
public class BlockingSocketConnector {
	private Protocol protocol;
	private IoListener listener;
	private IoWorker worker;
	private SocketIoSession session;
	private Executor executor;
	private Socket socket;
	private InputStream input;
	private OutputStream output;
	private boolean connected = false;

	public BlockingSocketConnector() {
		this(new NewThreadExecutor());
	}

	public BlockingSocketConnector(Executor executor) {
		if (executor == null) {
			throw new IllegalArgumentException("EXECUTOR");
		}
		this.executor = executor;
	}

	public void connect(InetSocketAddress remote, Protocol protocol, IoListener listener, SocketConfiguration configuration)
			throws IOException {
		if (socket != null) {
			throw new RuntimeException("Already connected");
		}
		if (protocol == null || listener == null || remote == null) {
			throw new IllegalArgumentException();
		}
		socket = new Socket(remote.getAddress(), remote.getPort());
		if (configuration != null) {
			socket.setKeepAlive(configuration.isKeepAlive());
			socket.setOOBInline(configuration.isOOBInline());
			socket.setReceiveBufferSize(configuration.getReceiveBufferSize());
			socket.setReuseAddress(configuration.isReuseAddress());
			socket.setSendBufferSize(configuration.getSendBufferSize());
			socket.setSoLinger(configuration.isLingerOn(), configuration.getLinger());
			socket.setSoTimeout(configuration.getTimeout());
			socket.setTcpNoDelay(configuration.isTcpNoDelay());
		}
		this.protocol = protocol;
		this.listener = listener;
		session = new SocketIoSession(null, null) {
			@Override
			public void close() {
				try {
					BlockingSocketConnector.this.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void write(Object message) {
				BlockingSocketConnector.this.write(message);
			}

			@Override
			public boolean isConnected() {
				return (socket != null && socket.isConnected());
			}

			@Override
			public void updateTrafficMask() {
			}

			@Override
			public int getWriteBufferSize() {
				try {
					return socket.getSendBufferSize();
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			}

			@Override
			public SocketAddress getRemoteAddress() {
				return socket.getRemoteSocketAddress();
			}
		};
		this.input = socket.getInputStream();
		this.output = socket.getOutputStream();
	}

	public void write(Object message) {
		EncoderOutputImpl ofb = new EncoderOutputImpl();
		protocol.getEncoder().encode(session, message, ofb);
		if (ofb.bb != null) {
			session.getWriteBufferQueue().add(new SocketWriteRequest(message, ofb.bb));
		}
	}

	public IoListener getListener() {
		return listener;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public SocketIoSession getSession() {
		return session;
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

	public void close() throws IOException {
		socket.close();
		socket = null;
		input.close();
		input = null;
		output.close();
		output = null;
	}

	class IoWorker
			implements Runnable {

		public void run() {
			byte[] buffer = new byte[2048];
			while (connected) {
				SocketWriteRequest req = session.getWriteBufferQueue().poll();
				req.getBuffer().capacity();
			}
		}

	}
}
