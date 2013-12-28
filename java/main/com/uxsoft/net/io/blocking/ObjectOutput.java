/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.blocking;

/**
 *
 * @author David
 */
public abstract class ObjectOutput {
	public abstract void writeObject( Object obj )
			throws Throwable;
}
