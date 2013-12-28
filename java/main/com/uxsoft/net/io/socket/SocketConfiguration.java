/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.socket;

/**
 *
 * @author David
 */
public class SocketConfiguration {
	private boolean keepAlive;
	private boolean lingerOn;
	private int linger;
	private boolean OOBInline;
	private int receiveBufferSize;
	private int sendBufferSize;
	private boolean reuseAddress;
	private int timeout;
	private boolean tcpNoDelay;

	public boolean isOOBInline() {
		return OOBInline;
	}

	public void setOOBInline(boolean OOBInline) {
		this.OOBInline = OOBInline;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public int getLinger() {
		return linger;
	}

	public void setLinger(int linger) {
		this.linger = linger;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	public boolean isReuseAddress() {
		return reuseAddress;
	}

	public void setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}

	public boolean isLingerOn() {
		return lingerOn;
	}

	public void setLingerOn(boolean lingerOn) {
		this.lingerOn = lingerOn;
	}
}
