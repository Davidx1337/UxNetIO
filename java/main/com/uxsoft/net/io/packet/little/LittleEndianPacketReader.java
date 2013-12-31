/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.packet.little;

import com.uxsoft.net.io.packet.PacketReader;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 *
 * @author David
 */
public class LittleEndianPacketReader implements PacketReader {
    private final byte[] bytes;
    private int pos = 0;

    public LittleEndianPacketReader(byte[] bytes) {
        this.bytes = bytes;
    }
    
    @Override
    public int readByte() {
        return bytes[pos++];
    }
    
    private int readByte2() {
        try {
            return bytes[pos++]&0xFF;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Reached end of bytestream! Pos=" + pos + ", Length=" + bytes.length);
        }
    }

    @Override
    public int readInt() {
        int byte1, byte2, byte3, byte4;

        byte1 = readByte2();
        byte2 = readByte2();
        byte3 = readByte2();
        byte4 = readByte2();
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    @Override
    public int readShort() {
        int byte1, byte2;

        byte1 = readByte2();
        byte2 = readByte2();
        return ((byte2 << 8) + byte1);
    }

    @Override
    public char readChar() {
        return (char) readShort();
    }

    @Override
    public long readLong() {
        long byte1 = readByte2();
        long byte2 = readByte2();
        long byte3 = readByte2();
        long byte4 = readByte2();
        long byte5 = readByte2();
        long byte6 = readByte2();
        long byte7 = readByte2();
        long byte8 = readByte2();

        return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16)
                + (byte2 << 8) + byte1;
    }

    public final String readAsciiString(int n) {
        char ret[] = new char[n];

        for (int x = 0; x < n; x++) {
            ret[x] = (char) readByte();
        }

        return String.valueOf(ret);
    }

    public final String readNullTerminatedAsciiString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b = 1;

        while (b != 0) {
            b = (byte)readByte();
            baos.write(b);
        }

        byte[] buf = baos.toByteArray();
        char[] chrBuf = new char[buf.length];

        for (int x = 0; x < buf.length; x++) {
            chrBuf[x] = (char) buf[x];
        }

        return String.valueOf(chrBuf);
    }

    public int getBytesRead() {
        return pos;
    }

    @Override
    public String readString() {
        return readAsciiString(readUnsignedShort());
    }

    public byte[] read(int num) {
        byte[] ret = new byte[num];

        for (int x = 0; x < num; x++) {
            ret[x] = bytes[pos++];
        }

        return ret;
    }

    public void skip(int num) {
        pos += num;
    }

    public int available() {
        return bytes.length - pos - 1;
    }

    @Override
    public String toString() {
        return Arrays.toString(bytes) + ", pos=" + pos;
    }

    public String readShortAsciiString() {
        return this.readAsciiString(this.readByte());
    }

    public int readUnsignedByte() {
        return readByte() & 0xFF;
    }

    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    public long readUnsignedInt() {
        return readInt() & 0xFFFFFFFF;
    }

    public long readUnsignedLong() {
        return 0;
        // TODO, java cant store 2^64 in primitive
    }

    public int read(byte[] buf) {
        for (int i = 0; i < buf.length; i++) {
            buf[i] = bytes[pos++];
        }
        
        return buf.length;
    }

    public int read(byte[] buf, int count) {
        for (int i = 0; i < count; i++) {
            buf[i] = bytes[pos++];
        }
        
        return count;
    }

    public int read(byte[] buf, int count, int offset) {
        for (int i = 0; i < count; i++) {
            buf[i+offset] = bytes[pos++];
        }
        
        return count;
    }

    public String readShortString() {
        int len = this.readUnsignedByte();
        return this.readAsciiString(len);
    }

    public String readLongString() {
        int len = this.readInt();
        return this.readAsciiString(len);
    }
}
