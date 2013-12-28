/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.blocking;

import com.uxsoft.net.io.common.IoSession;

/**
 *
 * @author David
 */
public interface BlockingProtocol {
	byte[] encodeObject( IoSession session, Object input, BlockingEncoderOutput output )
			throws Throwable;

	public void decodeObject ( IoSession session, byte[] input, int len, BlockingDecoderOutput output  )
			throws Throwable;
}
