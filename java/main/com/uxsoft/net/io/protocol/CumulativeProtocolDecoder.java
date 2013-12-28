/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.protocol;

import com.uxsoft.net.io.common.ByteBuffer;
import com.uxsoft.net.io.common.IoSession;

/**
 *
 * @author David
 */
public abstract class CumulativeProtocolDecoder
		implements ProtocolDecoder {
	private static final String StoreBufferKey = CumulativeProtocolDecoder.class.getName() + "_BUFFER_";

	public void dispose() {
	}

	public void decode(IoSession session, ByteBuffer message, DecoderOutput output) {
		boolean hasBuffer = false;
		ByteBuffer buf = (ByteBuffer) session.getAttribute(StoreBufferKey);
		if (buf != null) {
			hasBuffer = true;
			buf.put(message);
			buf.flip();
			//System.err.println("Already contains buffer");
		} else {
			buf = message;
			//System.err.println("No contained buffer");
		}

		while (true) {
			int oldPosition = buf.position();
			boolean decoded = doDecode(session, buf, output);
			if (decoded) {
				if (buf.position() == oldPosition) {
					throw new IllegalStateException("doDecode is returning true without reading data.");
				}

				if (!buf.hasRemaining()) {
					//System.err.println("Buffer consumed, breaking.");
					break;
				}
			} else {
				break;
			}
		}

		if (buf.hasRemaining()) {
			if (hasBuffer) {
				buf.compact();
				//System.err.println("Compacting buffer.");
			} else {
				ByteBuffer remainingBuf = ByteBuffer.allocate(buf.capacity());
				remainingBuf.order(buf.order());
				remainingBuf.put(buf);
				remainingBuf.setAutoExpand(true);
				session.setAttribute(StoreBufferKey, remainingBuf);
				//System.err.println("Storing remaining: " + remainingBuf.position() + " [" + buf.capacity() + "]");
			}
		} else if (hasBuffer) {
			ByteBuffer buffer = (ByteBuffer) session.removeAttribute(StoreBufferKey);
			if (buffer != null) {
				buffer.release();
			}
			//System.err.println("Destroying session buffer.");
		}
	}

	protected abstract boolean doDecode(IoSession session, ByteBuffer message, DecoderOutput output);
}
