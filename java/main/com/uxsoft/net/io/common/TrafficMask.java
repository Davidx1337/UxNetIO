/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.common;

import java.nio.channels.SelectionKey;

/**
 * Describes Traffic mask states.
 * Traffic masks provide a way to get the Interested Operations of a Key without blocking.
 * @author David
 */
public enum TrafficMask {
	READ(SelectionKey.OP_READ),                            // Accepting Recieve
	WRITE(SelectionKey.OP_WRITE),                          // Accepting Send
	ALL(SelectionKey.OP_READ | SelectionKey.OP_WRITE),     // Accepting All
	NONE(0);                                               // Accepting None

	private int i;

	private TrafficMask(int i) {
		this.i = i;
	}

	public int op() {
		return i;
	}

	public boolean canRead() {
		return (i & SelectionKey.OP_READ) == SelectionKey.OP_READ;
	}

	public boolean canWrite() {
		return (i & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE;
	}

	public static TrafficMask getInstance(int op) {
		boolean read = (op & SelectionKey.OP_READ) == SelectionKey.OP_READ;
		boolean write = (op & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE;
		if (read) {
			if (write) {
				return ALL;
			}
			return READ;
		} else if (write) {
			return WRITE;
		}
		return NONE;
	}

	public TrafficMask and(TrafficMask mask) {
		return getInstance(i & mask.i);
	}

	public TrafficMask or(TrafficMask mask) {
		return getInstance(i | mask.i);
	}

	public TrafficMask xor(TrafficMask mask) {
		return getInstance(i ^ mask.i);
	}

	public TrafficMask not() {
		return getInstance(~i);
	}

	public int getInterestOps() {
		return i;
	}
}
