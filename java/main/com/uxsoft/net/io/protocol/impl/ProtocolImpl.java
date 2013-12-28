/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.protocol.impl;

import com.uxsoft.net.io.protocol.Protocol;
import com.uxsoft.net.io.protocol.ProtocolDecoder;
import com.uxsoft.net.io.protocol.ProtocolEncoder;

/**
 * Implementation for {@link com.uxsoft.net.io.protocol.Protocol}.
 *
 * @author David
 */
public class ProtocolImpl
        implements Protocol {

    private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;

    public ProtocolImpl(ProtocolEncoder encoder, ProtocolDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public ProtocolDecoder getDecoder() {
        return decoder;
    }

    public ProtocolEncoder getEncoder() {
        return encoder;
    }
}
