/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.common;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * IoSession is an interface giving methods to interact with clients or servers.
 * @author David
 */
public interface IoSession {
	/**
	 * Writes a message to this <tt>IoSession</tt>.
	 * @param message
	 */
	void write(Object message);

	/**
	 * Gets the <tt>SelectionKey</tt> for this <tt>IoSession</tt>.
	 * @return
	 */
	SelectionKey getSelectionKey();

	/**
	 * Gets the <tt>SocketChannel</tt> for this <tt>IoSession</tt>.
	 * @return
	 */
	SocketChannel getSocketChannel();

	/**
	 * Gets the amount of bytes this session has sent.
	 * @return
	 */
	int getSentBytes();

	/**
	 * Gets the amount of bytes this session has recieved.
	 * @return
	 */
	int getReceivedBytes();

	/**
	 * Gets the total amount of bytes this session has processed.
	 * @return
	 */
	int getTotalBytes();

	/**
	 * Gets the traffic mask for this session.
	 * @return
	 */
	TrafficMask getTrafficMask();

	/**
	 * Removes the attribute for this session
	 * @param key
	 * @return
	 */
	Object removeAttribute(String key);

	/**
	 * Returns whether this session has the specified attribute
	 * @param key
	 * @return
	 */
	boolean hasAttribute(String key);

	/**
	 * Sets an attribute on this sesson
	 * @param key
	 * @param o
	 */
	void setAttribute(String key, Object o);

	/**
	 * Gets an attribute for this session
	 * @param key
	 * @return
	 */
	Object getAttribute(String key);

	/**
	 * Gets the remote address for this session
	 * @return
	 */
	SocketAddress getRemoteAddress();

	/**
	 * Closes this session
	 */
	void close();

	/**
	 * Returns whether this session is connected
	 * @return
	 */
	boolean isConnected();
}
