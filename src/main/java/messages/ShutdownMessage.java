package main.java.messages;

import java.io.Serializable;
import java.util.Vector;

/**
 * Sent by a slave to tell the master of its shutdown and the reason for it
 * @author thomasm
 *
 */
public class ShutdownMessage implements Serializable {
	
	private String reason;
	private String hostAddress;

	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
