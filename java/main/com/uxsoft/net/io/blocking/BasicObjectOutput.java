/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.blocking;

/**
 *
 * @author David
 */
public class BasicObjectOutput
		extends ObjectOutput {

	private Object o = null;

	@Override
	public void writeObject(Object obj) throws Throwable {
		o = obj;
	}

	public Object get() {
		return o;
	}

}
