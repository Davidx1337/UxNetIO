/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.socket;

import com.uxsoft.net.io.common.ByteBuffer;
import com.uxsoft.net.io.common.WriteRequest;

/**
 *
 * @author David
 */
public class SocketWriteRequest
		implements WriteRequest {
	private Object message;
	private ByteBuffer buffer;

	public SocketWriteRequest(Object message, ByteBuffer buffer) {
		this.message = message;
		this.buffer = buffer;
	}

	public Object getMessage() {
		return message;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
}
