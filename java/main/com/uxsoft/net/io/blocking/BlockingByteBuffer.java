/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.blocking;

/**
 *
 * @author David
 */
public class BlockingByteBuffer {
	private byte[] buf;
	private int pos;

	public BlockingByteBuffer() {
		this(8192);
	}

	public BlockingByteBuffer(int size) {
		buf = new byte[size];
		pos = 0;
	}

	private void expand(int extra) {
		if (buf.length < pos + extra) {
			byte[] newBytes = new byte[buf.length * 2];
			System.arraycopy(buf, 0, newBytes, 0, pos);
			buf = newBytes;
		}
	}

	public void putByte(byte v) {
		expand(1);
		buf[pos++] = v;
	}

	public void putShort(short v) {
		expand(2);
		buf[pos++] = (byte) ((v >>> 8) & 0xFF);
		buf[pos++] = (byte) ((v) & 0xFF);
	}

	public void putInt(int v) {
		expand(4);
		buf[pos++] = (byte) ((v >>> 24) & 0xFF);
		buf[pos++] = (byte) ((v >>> 16) & 0xFF);
		buf[pos++] = (byte) ((v >>> 8) & 0xFF);
		buf[pos++] = (byte) ((v) & 0xFF);
	}

	public void putLong(long v) {
		expand(8);
		buf[pos++] = (byte) ((v >>> 56) & 0xFF);
		buf[pos++] = (byte) ((v >>> 48) & 0xFF);
		buf[pos++] = (byte) ((v >>> 40) & 0xFF);
		buf[pos++] = (byte) ((v >>> 32) & 0xFF);
		buf[pos++] = (byte) ((v >>> 24) & 0xFF);
		buf[pos++] = (byte) ((v >>> 16) & 0xFF);
		buf[pos++] = (byte) ((v >>> 8) & 0xFF);
		buf[pos++] = (byte) ((v) & 0xFF);
	}

	public void putFloat(float v) {
		putInt(Float.floatToIntBits(v));
	}

	public void putDouble(double v) {
		putLong(Double.doubleToLongBits(v));
	}

	public void putChar(char v) {
		putShort((short) (v));
	}

	public void put(byte[] bytes) {
		put(bytes, bytes.length);
	}

	public void put(byte[] bytes, int len) {
		if (len > bytes.length) {
			throw new IllegalArgumentException("len > bytes.len");
		}
		expand(len);
		System.arraycopy(bytes, 0, buf, pos, len);
		pos += len;
	}

	public int position() {
		return pos;
	}

	public int capacity() {
		return buf.length;
	}

	public byte getByte() {
		return buf[pos--];
	}

	public short getShort() {
		short bytes = (short) (buf[pos - 1] << 8);
		bytes |= (buf[pos] & 0xFF);
		pos -= 2;
		return bytes;
	}

	public int getInt() {
		return (int) ((buf[pos--] & 0xff) + ((buf[pos--] & 0xff) << 8) + ((buf[pos--] & 0xff) << 16) + ((buf[pos--] & 0xff) << 24));
	}

	public long getLong() {
		long l1 = (buf[pos--] & 0xff) << 56;
		long l2 = (buf[pos--] & 0xff) << 48;
		long l3 = (buf[pos--] & 0xff) << 40;
		long l4 = (buf[pos--] & 0xff) << 32;
		long l5 = (buf[pos--] & 0xff) << 24;
		long l6 = (buf[pos--] & 0xff) << 16;
		long l7 = (buf[pos--] & 0xff) << 8;
		long l8 = (buf[pos--] & 0xff);
		return l1+l2+l3+l4+l5+l6+l7+l8;
	}

	public byte getByte(int pos) {
		return buf[pos--];
	}

	public short getShort(int pos) {
		short bytes = (short) (buf[pos - 1] << 8);
		bytes |= (buf[pos] & 0xFF);
		return bytes;
	}

	public int getInt(int pos) {
		return ((buf[pos--] << 24) + (buf[pos--] << 16) + (buf[pos--] << 8) + (buf[pos--]));
	}

	public long getLong(int pos) {
		long l1 = (buf[pos--] & 0xff) << 56;
		long l2 = (buf[pos--] & 0xff) << 48;
		long l3 = (buf[pos--] & 0xff) << 40;
		long l4 = (buf[pos--] & 0xff) << 32;
		long l5 = (buf[pos--] & 0xff) << 24;
		long l6 = (buf[pos--] & 0xff) << 16;
		long l7 = (buf[pos--] & 0xff) << 8;
		long l8 = (buf[pos--] & 0xff);
		return l1+l2+l3+l4+l5+l6+l7+l8;
	}

	public float getFloat() {
		return Float.intBitsToFloat(getInt());
	}

	public double getDouble() {
		return Double.longBitsToDouble(getLong());
	}

	public int get(byte[] bytes) {
		int len = Math.min(bytes.length, pos);
		System.arraycopy(buf, pos - len, bytes, 0, len);
		pos -= len;
		return len;
	}

	public byte[] get(int len) {
		if (len > pos) {
			throw new IllegalArgumentException("len > pos");
		}
		byte[] ret = new byte[len];
		System.arraycopy(buf, pos - len, ret, 0, len);
		pos -= len;
		return ret;
	}

	public byte[] empty() {
		return get(pos);
	}

	public void put(BlockingByteBuffer other) {
		this.put(other.buf);
	}

	public boolean hasRemaining() {
		return pos > 0;
	}

	public boolean compact() {
		return false;
	}
	
	public int remaining() {
		return pos;
	}
	
	public int getUnsignedByte(int pos) {
		return getByte(pos) & 0xff;
	}
	
	public int getUnsignedShort(int pos) {
		return getShort(pos) & 0xffff;
	}
	
	public int getUnsignedByte() {
		return getByte() & 0xff;
	}
	
	public int getUnsignedShort() {
		return getShort() & 0xffff;
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
}
