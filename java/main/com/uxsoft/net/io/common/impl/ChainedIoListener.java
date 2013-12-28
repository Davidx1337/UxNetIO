/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.common.impl;

import java.util.Iterator;
import java.util.LinkedList;
import com.uxsoft.net.io.common.IoListener;
import com.uxsoft.net.io.common.IoSession;

/**
 * A <tt>ChainedIoListener</tt> provides a class to chain lots of {@link IoListener}s into a group, so
 * when an event happens all the registered listeners are notified.
 * @author David
 */
public class ChainedIoListener
		implements IoListener {
	private LinkedList<IoListener> listeners = new LinkedList();

	public void dispose() {
		listeners.clear();
		listeners = null;
	}

	public void registerListener(IoListener listener) {
		listeners.addLast(listener);
	}

	public void clearListeners() {
		listeners.clear();
	}

	public void onSessionCreated(IoSession session)
			throws Exception {
		Iterator<IoListener> listenerItr = listeners.iterator();
		while (listenerItr.hasNext()) {
			listenerItr.next().onSessionCreated(session);
		}
	}

	public void onSessionOpened(IoSession session)
			throws Exception {
		Iterator<IoListener> listenerItr = listeners.iterator();
		while (listenerItr.hasNext()) {
			listenerItr.next().onSessionOpened(session);
		}
	}

	public void onMessageSent(IoSession session, Object message)
			throws Exception {
		Iterator<IoListener> listenerItr = listeners.iterator();
		while (listenerItr.hasNext()) {
			listenerItr.next().onMessageSent(session, message);
		}
	}

	public void onMessageReceived(IoSession session, Object message)
			throws Exception {
		Iterator<IoListener> listenerItr = listeners.iterator();
		while (listenerItr.hasNext()) {
			listenerItr.next().onMessageReceived(session, message);
		}
	}

	public void onSessionClosed(IoSession session)
			throws Exception {
		Iterator<IoListener> listenerItr = listeners.iterator();
		while (listenerItr.hasNext()) {
			listenerItr.next().onSessionClosed(session);
		}
	}

	public void onExceptionOccurred(IoSession session, Throwable ex)
			throws Exception {
		Iterator<IoListener> listenerItr = listeners.iterator();
		while (listenerItr.hasNext()) {
			listenerItr.next().onExceptionOccurred(session, ex);
		}
	}
}
