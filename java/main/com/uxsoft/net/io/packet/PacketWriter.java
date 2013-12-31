/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.packet;

import com.uxsoft.net.io.IntValueHolder;
import com.uxsoft.net.io.UxPacket;

/**
 *
 * @author David
 */
public interface PacketWriter {

    void writeByte(int value);

    void writeShort(int value);

    void writeInt(int value);

    void writeLong(long value);

    void writeUnsignedByte(int value);

    void writeUnsignedShort(int value);

    void writeUnsignedInt(long value);

    void writeUnsignedLong(long value);
    
    void write(int b);

    void write(byte[] buf);

    void write(byte[] buf, int count);

    void write(byte[] buf, int count, int offset);

    void writeString(String string);

    void writeShortString(String string);

    void writeLongString(String string);
    
    UxPacket getPacket();
    
    byte[] getBytes();
    
    void writeHeader(IntValueHolder h);
}
