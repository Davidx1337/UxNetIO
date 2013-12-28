/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.common;

import java.nio.ByteOrder;

/**
 *
 * @author David
 */
public class ByteBuffer {
	private static boolean useDirectDefault = false;
	private java.nio.ByteBuffer internal;
	private boolean autoExpand = false;
	private int mark = -1;

	public ByteBuffer(java.nio.ByteBuffer buffer) {
		buffer.order(ByteOrder.BIG_ENDIAN);
		this.internal = buffer;
	}

	public static boolean isUseDirectDefault() {
		return useDirectDefault;
	}

	public static void setUseDirectDefault(boolean useDirectDefault) {
		ByteBuffer.useDirectDefault = useDirectDefault;
	}

	public static ByteBuffer allocate(int size) {
		if (useDirectDefault) {
			try {
				return allocateDirect(size);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return allocateHeap(size);
	}

	public static ByteBuffer allocateHeap(int size) {
		return new ByteBuffer(java.nio.ByteBuffer.allocate(size));
	}

	public static ByteBuffer allocateDirect(int size) {
		return new ByteBuffer(java.nio.ByteBuffer.allocateDirect(size));
	}

	public static ByteBuffer wrap(byte[] bytes) {
		return wrap(bytes, 0, bytes.length);
	}

	public static ByteBuffer wrap(byte[] bytes, int off, int len) {
		return new ByteBuffer(java.nio.ByteBuffer.wrap(bytes, off, len));
	}

	public static ByteBuffer wrap(java.nio.ByteBuffer buf) {
		return new ByteBuffer(buf);
	}

	public void unmark() {
		mark = -1;
	}

	public ByteBuffer position(int newPos) {
		internal.position(newPos);
		if (newPos < mark) {
			mark = -1;
		}
		return this;
	}

	public int position() {
		return internal.position();
	}

	public ByteBuffer putInt(int position, int value) {
		autoExpand(4);
		internal.putInt(position, value);
		return this;
	}

	public ByteBuffer putInt(int value) {
		autoExpand(4);
		internal.putInt(value);
		return this;
	}

	public int getInt(int position) {
		return internal.getInt(position);
	}

	public int getInt() {
		return internal.getInt();
	}

	public ByteBuffer putLong(int position, long value) {
		autoExpand(8);
		internal.putLong(position, value);
		return this;
	}

	public ByteBuffer putLong(long value) {
		autoExpand(8);
		internal.putLong(value);
		return this;
	}

	public long getLong(int position) {
		return internal.getLong(position);
	}

	public long getLong() {
		return internal.getLong();
	}

	public ByteBuffer putShort(int position, short value) {
		autoExpand(2);
		internal.putShort(position, value);
		return this;
	}

	public ByteBuffer putShort(short value) {
		autoExpand(2);
		internal.putShort(value);
		return this;
	}

	public short getShort(int position) {
		return internal.getShort(position);
	}

	public short getShort() {
		return internal.getShort();
	}

	public ByteBuffer putChar(int position, char value) {
		autoExpand(2);
		internal.putChar(position, value);
		return this;
	}

	public ByteBuffer putChar(char value) {
		autoExpand(2);
		internal.putChar(value);
		return this;
	}

	public char getChar(int position) {
		return internal.getChar(position);
	}

	public char getChar() {
		return internal.getChar();
	}

	public ByteBuffer putByte(int position, byte value) {
		autoExpand(1);
		internal.put(position, value);
		return this;
	}

	public ByteBuffer putByte(byte value) {
		autoExpand(1);
		internal.put(value);
		return this;
	}

	public byte getByte(int position) {
		return internal.get(position);
	}

	public byte getByte() {
		return internal.get();
	}

	public ByteBuffer putUnsignedShort(int position, int value) {
		putShort(position, ((short) (value & 0xFFFF)));
		return this;
	}

	public ByteBuffer putUnsignedShort(int value) {
		putShort((short) (value & 0xFFFF));
		return this;
	}

	public int getUnsignedShort(int position) {
		return getShort(position) & 0xFFFF;
	}

	public int getUnsignedShort() {
		return getShort() & 0xFFFF;
	}

	public ByteBuffer putUnsignedByte(int position, byte value) {
		putByte(position, (byte) (value & 0xFF));
		return this;
	}

	public ByteBuffer putUnsignedByte(byte value) {
		putByte((byte) (value & 0xFF));
		return this;
	}

	public int getUnsignedByte(int position) {
		return getByte(position) & 0xFF;
	}

	public int getUnsignedByte() {
		return getByte() & 0xFF;
	}

	public ByteBuffer flip() {
		internal.flip();
		return this;
	}

	public ByteBuffer slice() {
		return new ByteBuffer(internal.slice());
	}

	public boolean isDirect() {
		return internal.isDirect();
	}

	public boolean isReadOnly() {
		return internal.isReadOnly();
	}

	public byte[] array() {
		return internal.array();
	}

	public int arrayOffset() {
		return internal.arrayOffset();
	}

	public boolean hasArray() {
		return internal.hasArray();
	}

	public java.nio.ByteBuffer buf() {
		return internal;
	}

	public int remaining() {
		return limit() - position();
	}

	public int limit() {
		return internal.limit();
	}

	public boolean hasRemaining() {
		return remaining() > 0;
	}

	public ByteBuffer put(byte[] bytes, int off, int len) {
		autoExpand(len);
		internal.put(bytes, off, len);
		return this;
	}

	public ByteBuffer put(byte[] bytes) {
		return put(bytes, 0, bytes.length);
	}

	public ByteBuffer put(java.nio.ByteBuffer buf) {
		autoExpand(buf.remaining());
		internal.put(buf);
		return this;
	}

	public ByteOrder order() {
		return internal.order();
	}

	public ByteBuffer order(ByteOrder order) {
		internal.order(order);
		return this;
	}

	public int capacity() {
		return internal.capacity();
	}

	public ByteBuffer put(ByteBuffer buf) {
		autoExpand(buf.remaining());
		internal.put(buf.buf());
		return this;
	}

	public ByteBuffer compact() {
		internal.compact();
		return this;
	}

	public ByteBuffer mark() {
		internal.mark();
		mark = position();
		return this;
	}

	public ByteBuffer rewind() {
		internal.rewind();
		return this;
	}

	public ByteBuffer reset() {
		internal.reset();
		return this;
	}

	public ByteBuffer get(byte bytes[]) {
		internal.get(bytes);
		return this;
	}

    public boolean prefixedDataAvailable(int prefixLength) {
        return prefixedDataAvailable(prefixLength, Integer.MAX_VALUE);
    }

    public boolean prefixedDataAvailable(int prefixLength, int maxDataLength) {
        if (remaining() < prefixLength) {
            return false;
        }

        int dataLength;
        switch (prefixLength) {
        case 1:
            dataLength = getUnsignedByte(position());
            break;
        case 2:
            dataLength = getUnsignedShort(position());
            break;
        case 4:
            dataLength = getInt(position());
            break;
        default:
            throw new IllegalArgumentException("prefixLength: " + prefixLength);
        }

        if (dataLength < 0 || dataLength > maxDataLength) {
            throw new RuntimeException("dataLength: " + dataLength);
        }

        return remaining() - prefixLength >= dataLength;
    }

	public void release() {

	}

	public void acquire() {
		
	}

    public ByteBuffer capacity(int newCapacity) {
        if (newCapacity > capacity()) {
            // Allocate a new buffer and transfer all settings to it.
            int pos = position();
            int limit = limit();
            ByteOrder bo = order();

            capacity0(newCapacity);
            buf().limit(limit);
            if (mark >= 0) {
                buf().position(mark);
                buf().mark();
            }
            buf().position(pos);
            buf().order(bo);
        }

        return this;
    }
	
	protected void capacity0(int requestedCapacity) {
		int newCapacity = 1;
		while (newCapacity < requestedCapacity) {
			newCapacity <<= 1;
		}

		java.nio.ByteBuffer oldBuf = this.internal;
		java.nio.ByteBuffer newBuf;
		if (isDirect()) {
			newBuf = java.nio.ByteBuffer.allocateDirect(newCapacity);
		} else {
			newBuf = java.nio.ByteBuffer.allocate(newCapacity);
		}

		newBuf.clear();
		oldBuf.clear();
		newBuf.put(oldBuf);
		this.internal = newBuf;
	}

    protected ByteBuffer autoExpand(int expectedRemaining) {
        if (isAutoExpand()) {
            expand(expectedRemaining);
        }
        return this;
    }

    protected ByteBuffer autoExpand(int pos, int expectedRemaining) {
        if (isAutoExpand()) {
            expand(pos, expectedRemaining);
        }
        return this;
    }

    public ByteBuffer expand(int pos, int expectedRemaining) {
        int end = pos + expectedRemaining;
        if (end > capacity()) {
            capacity(end);
        }

        if (end > limit()) {
            buf().limit(end);
        }
        return this;
    }

    public ByteBuffer expand(int expectedRemaining) {
        return expand(position(), expectedRemaining);
    }

	public boolean isAutoExpand() {
		return autoExpand;
	}

	public void setAutoExpand(boolean autoExpand) {
		this.autoExpand = autoExpand;
	}

	public byte[] getBytes() {
		return internal.array();
	}
}
