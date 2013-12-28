/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.packet.little;

import com.uxsoft.net.io.ByteArrayPacket;
import com.uxsoft.net.io.UxPacket;
import com.uxsoft.net.io.packet.PacketWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author David
 */
public class LittleEndianPacketWriter
        implements PacketWriter {

    private ByteArrayOutputStream baos;

    public LittleEndianPacketWriter() {
        this(32);
    }

    public LittleEndianPacketWriter(int size) {
        baos = new ByteArrayOutputStream(size);
    }

    public void writeByte(int value) {
        baos.write(value);
    }

    public void writeShort(int value) {
        baos.write((byte) (value & 0xFF));
        baos.write((byte) ((value >>> 8) & 0xFF));
    }

    public void writeInt(int value) {
        baos.write((byte) (value & 0xFF));
        baos.write((byte) ((value >>> 8) & 0xFF));
        baos.write((byte) ((value >>> 16) & 0xFF));
        baos.write((byte) ((value >>> 24) & 0xFF));
    }

    public void writeLong(long value) {
        baos.write((byte) (value & 0xFF));
        baos.write((byte) ((value >>> 8) & 0xFF));
        baos.write((byte) ((value >>> 16) & 0xFF));
        baos.write((byte) ((value >>> 24) & 0xFF));
        baos.write((byte) ((value >>> 32) & 0xFF));
        baos.write((byte) ((value >>> 40) & 0xFF));
        baos.write((byte) ((value >>> 48) & 0xFF));
        baos.write((byte) ((value >>> 56) & 0xFF));
    }

    public void writeUnsignedByte(int value) {
        baos.write((byte) (value & 0xff));
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
        try {
            baos.write(buf);
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    public void write(byte[] buf, int count) {
        baos.write(buf, 0, count);
    }

    public void write(byte[] buf, int count, int offset) {
        baos.write(buf, offset, count);
    }

    public void writeString(String string) {
        writeShort(string.length());
        write(string.getBytes());
    }

    public void writeShortString(String string) {
        writeByte(string.length());
        write(string.getBytes());
    }

    public void writeLongString(String string) {
        writeLong(string.length());
        write(string.getBytes());
    }

    public UxPacket getPacket() {
        return new ByteArrayPacket(baos.toByteArray());
    }
}
