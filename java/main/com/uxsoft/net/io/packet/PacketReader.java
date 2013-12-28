/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.packet;

/**
 *
 * @author David
 */
public interface PacketReader {

    int readByte();

    int readShort();

    int readInt();

    long readLong();

    int readUnsignedByte();

    int readUnsignedShort();

    long readUnsignedInt();

    long readUnsignedLong();
    
    char readChar();

    int read(byte[] buf);

    int read(byte[] buf, int count);

    int read(byte[] buf, int count, int offset);

    String readString();

    String readShortString();

    String readLongString();

    int available();
}
