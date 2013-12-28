/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.protocol;

import com.uxsoft.net.io.common.ByteBuffer;

/**
 *
 * @author David
 */
public interface EncoderOutput {
	void write(ByteBuffer out);
}
