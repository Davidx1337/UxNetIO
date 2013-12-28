/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.common;

/**
 *
 * @author David
 */
public abstract class IoListenerAdapter
		implements IoListener {
	public void onExceptionOccurred(IoSession session, Throwable ex) throws Exception {

	}

	public void onMessageReceived(IoSession session, Object message) throws Exception {

	}

	public void onMessageSent(IoSession session, Object message) throws Exception {

	}

	public void onSessionClosed(IoSession session) throws Exception {

	}

	public void onSessionCreated(IoSession session) throws Exception {

	}

	public void onSessionOpened(IoSession session) throws Exception {
		
	}
}
