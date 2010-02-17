package main.java.messages;

import java.io.Serializable;

/**
 * Tells the slave to shut down
 * @author thomasm
 *
 */
public class SlaveShutdownMessage implements Serializable {
	private String reason;

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
