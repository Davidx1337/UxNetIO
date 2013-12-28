/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.protocol;

import com.uxsoft.net.io.common.ByteBuffer;
import com.uxsoft.net.io.common.IoSession;

/**
 * A <tt>ProtocolDecoder</tt> is a class for decoding protocol-specific data into Java Objects.
 * @author David
 */
public interface ProtocolDecoder {
	void dispose();

	void decode(IoSession session, ByteBuffer message, DecoderOutput output);
}
