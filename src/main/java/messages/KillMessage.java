package main.java.messages;

import java.io.Serializable;

public class KillMessage implements Serializable {
	private String reason;

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
