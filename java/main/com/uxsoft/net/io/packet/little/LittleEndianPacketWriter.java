/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.packet.little;

import com.uxsoft.net.io.ByteArrayPacket;
import com.uxsoft.net.io.IntValueHolder;
import com.uxsoft.net.io.UxPacket;
import com.uxsoft.net.io.packet.PacketWriter;
import java.util.Arrays;

/**
 *
 * @author David
 */
public class LittleEndianPacketWriter
        implements PacketWriter {

    private byte[] bytes;
    private int pos = 0;

    public LittleEndianPacketWriter() {
        this(32);
    }

    public LittleEndianPacketWriter(int size) {
        bytes = new byte[size];
    }

    private void dontOverflow(int zz) {
        if (pos+zz >= bytes.length) {
            int xo = bytes.length << 1;
            bytes = Arrays.copyOf(bytes, xo);
        }
    }

    public void writeByte(int value) {
        dontOverflow(1);
        bytes[pos++] = (byte) value;
    }

    public void writeShort(int value) {
        dontOverflow(2);
        bytes[pos++] = ((byte) (value & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 8) & 0xFF));
    }

    public void writeInt(int value) {
        dontOverflow(4);
        bytes[pos++] = ((byte) (value & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 8) & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 16) & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 24) & 0xFF));
    }

    public void writeLong(long value) {
        dontOverflow(8);
        bytes[pos++] = ((byte) (value & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 8) & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 16) & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 24) & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 32) & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 40) & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 48) & 0xFF));
        bytes[pos++] = ((byte) ((value >>> 56) & 0xFF));
    }

    public void writeUnsignedByte(int value) {
        dontOverflow(1);
        bytes[pos++] = ((byte) (value & 0xff));
    }

    public void writeUnsignedShort(int value) {
        writeShort((short) (value & 0xffff));
    }

    public void writeUnsignedInt(long value) {
        writeInt((int) (value & 0xffffffff));
    }

    public void writeUnsignedLong(long value) {
        throw new RuntimeException();
    }

    public void write(byte[] buf) {
        write(buf, buf.length);
    }

    public void write(byte[] buf, int count) {
        dontOverflow(count);
        for (int i = 0; i < count; i++) {
            bytes[pos++] = buf[i];
        }
    }

    public void write(byte[] buf, int count, int offset) {
        dontOverflow(count);
        for (int i = 0; i < count; i++) {
            bytes[pos++] = buf[i + offset];
        }
    }

    public void writeString(String string) {
        dontOverflow(2+string.length());
        writeShort(string.length());
        write(string.getBytes());
    }

    public void writeShortString(String string) {
        dontOverflow(1+string.length());
        writeByte(string.length());
        write(string.getBytes());
    }

    public void writeLongString(String string) {
        dontOverflow(4+string.length());
        writeLong(string.length());
        write(string.getBytes());
    }

    public UxPacket getPacket() {
        return new ByteArrayPacket(Arrays.copyOf(bytes, pos));
    }

    public void write(int b) {
        dontOverflow(1);
        bytes[pos++] = (byte) (b);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, pos);
    }

    public void writeHeader(IntValueHolder h) {
        write(h.getValue());
    }
}
