/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.protocol;

import com.uxsoft.net.io.common.IoSession;

/**
 * A <tt>ProtocolEncoder</tt> is a class for encoding Java Objects into Network stream bytes.
 * @author David
 */
public interface ProtocolEncoder {
	void dispose();

	void encode(IoSession session, Object message, EncoderOutput output);
}
