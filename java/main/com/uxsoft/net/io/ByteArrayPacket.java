/*
 * This file is part of the "Renoria" Game.
 * Copyright (C) 2008
 * IDGames.
 */

package com.uxsoft.net.io;

//~--- non-JDK imports --------------------------------------------------------

import com.uxsoft.net.io.in.ByteArrayByteStream;
import com.uxsoft.net.io.in.GenericSeekableLittleEndianAccessor;
import com.uxsoft.net.io.in.SeekableLittleEndianAccessor;
import com.uxsoft.net.out.PacketWriter;

/**
 * Represents a byte array packet.
 * @author David
 */
public class ByteArrayPacket implements UxPacket {
    private byte[] data;

    public ByteArrayPacket(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getBytes() {
		if (plew != null) {
			return plew.getBytes();
		}
        return data;
    }
}
