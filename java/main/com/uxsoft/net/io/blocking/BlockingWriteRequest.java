/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.blocking;

/**
 *
 * @author David
 */
public interface BlockingWriteRequest {
	byte[] getBytes();

	Object getMessage();

	void dispose();
}
