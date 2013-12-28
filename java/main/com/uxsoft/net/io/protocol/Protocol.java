/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.protocol;

/**
 * An interface to describe a specific protocol.
 * @author David
 */
public interface Protocol {
	ProtocolEncoder getEncoder();

	ProtocolDecoder getDecoder();
}
