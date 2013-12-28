/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.common;

/**
 * IoListener is an interface used to listen for events from servers or clients.
 * @author David
 */
public interface IoListener {
	void onSessionCreated(IoSession session) throws Exception;

	void onSessionOpened(IoSession session) throws Exception;

	void onMessageSent(IoSession session, Object message) throws Exception;

	void onMessageReceived(IoSession session, Object message) throws Exception;

	void onSessionClosed(IoSession session) throws Exception;

	void onExceptionOccurred(IoSession session, Throwable ex) throws Exception;
}
