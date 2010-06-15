package main.java.messages;

import java.io.Serializable;

/**
 * Sent by the master to order a shutdown of the slave
 * @author thomasm
 *
 */
public class KillMessage implements Serializable {
	private String reason;

	public KillMessage(String reason) {
		this.reason = reason;
	}
	
	public String getReason() {
		return reason;
	}

}
