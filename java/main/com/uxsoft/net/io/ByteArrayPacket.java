/*
 * This file is part of the "Renoria" Game.
 * Copyright (C) 2008
 * IDGames.
 */
package com.uxsoft.net.io;

//~--- non-JDK imports --------------------------------------------------------
/**
 * Represents a byte array packet.
 *
 * @author David
 */
public class ByteArrayPacket implements UxPacket {

    private byte[] data;

    public ByteArrayPacket(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getBytes() {
        return data;
    }
}
