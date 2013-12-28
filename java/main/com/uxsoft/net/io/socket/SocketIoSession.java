/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.socket;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.uxsoft.net.io.common.IoSession;
import com.uxsoft.net.io.common.TrafficMask;
import com.uxsoft.net.io.socket.SocketAcceptor.IoProcessor;

/**
 *
 * @author David
 */
public class SocketIoSession
		implements IoSession {
	private SocketChannel channel;
	private SelectionKey key;
	private int
			sentBytes = 0,
			receivedBytes = 0;
	private int readBufferSize = 1024;
	private boolean flushQueued = false;
	private IoProcessor processor;
	private TrafficMask mask = TrafficMask.READ;
	private HashMap<String, Object> attributes = new HashMap();
	private Queue<SocketWriteRequest> writeBufferQueue = new ConcurrentLinkedQueue();
	private boolean trafficUpdateRequested = false;

	public SocketIoSession(SocketChannel channel, IoProcessor processor) {
		this.channel = channel;
		this.processor = processor;
	}

	public boolean isTrafficUpdateRequested() {
		return trafficUpdateRequested;
	}

	public void setTrafficUpdateRequested(boolean trafficUpdateRequested) {
		this.trafficUpdateRequested = trafficUpdateRequested;
	}

	public Queue<SocketWriteRequest> getWriteBufferQueue() {
		return writeBufferQueue;
	}

	public IoProcessor getProcessor() {
		return processor;
	}

	public void updateTrafficMask() {
		processor.updateTrafficMask(this);
	}

	public void setKey(SelectionKey key) {
		this.key = key;
	}

	public boolean isFlushQueued() {
		return flushQueued;
	}

	public void setFlushQueued(boolean flushQueued) {
		this.flushQueued = flushQueued;
	}

	public int getReadBufferSize() {
		return readBufferSize;
	}

	public int getWriteBufferSize() {
		try {
			return channel.socket().getSendBufferSize();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public SelectionKey getSelectionKey() {
		return key;
	}

	public SocketChannel getSocketChannel() {
		return channel;
	}

	public int getSentBytes() {
		return sentBytes;
	}

	public int getReceivedBytes() {
		return receivedBytes;
	}

	public int getTotalBytes() {
		return sentBytes + receivedBytes;
	}

	public void increaseSentBytes(int inc) {
		sentBytes += inc;
	}

	public void increaseReceivedBytes(int inc) {
		receivedBytes += inc;
	}

	public TrafficMask getTrafficMask() {
		return mask;
	}

	public Object removeAttribute(String key) {
		return attributes.remove(key);
	}

	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	public void setAttribute(String key, Object o) {
		attributes.put(key, o);
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public void setTrafficMask(TrafficMask mask) {
		if (mask == null) {
			throw new IllegalArgumentException();
		}
		if (mask == this.mask) {
			return;
		}
		this.mask = mask;
		updateTrafficMask();
	}

    public void suspendRead() {
        setTrafficMask(getTrafficMask().and(TrafficMask.READ.not()));
    }

    public void suspendWrite() {
        setTrafficMask(getTrafficMask().and(TrafficMask.WRITE.not()));
    }

    public void resumeRead() {
        setTrafficMask(getTrafficMask().or(TrafficMask.READ));
    }

    public void resumeWrite() {
		setTrafficMask(getTrafficMask().or(TrafficMask.WRITE));
    }

	public void write(Object message) {
		//System.err.println("write() invoked");
		writeBufferQueue.add(new SocketWriteRequest(message, processor.encodeObject(this, message)));
		if (mask.canWrite()) {
			processor.flushSession(this);
		} else {
			resumeWrite();
		}
	}

	public SocketAddress getRemoteAddress() {
		return channel.socket().getRemoteSocketAddress();
	}

	public void close() {
		processor.scheduleRemove(this);
	}

	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}
}
