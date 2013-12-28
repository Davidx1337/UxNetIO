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
public interface BlockingDecoder {
	void decode(IoSession session, byte [] message, Output output);

	public static interface Output {
		void write(Object obj);
	}
}
