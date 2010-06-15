package main.java.messages;

import java.io.Serializable;

/**
 * Tells the slave to shut down
 * @author thomasm
 *
 */
public class SlaveShutdownMessage implements Serializable {
	private String reason;

	public void SlaveShutdownMessage(String reason) {
		this.reason = reason;
	}
	
	public String getReason() {
		return reason;
	}
}
