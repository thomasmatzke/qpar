package main.java.messages;

import java.io.Serializable;
import java.util.Vector;

public class ShutdownMessage implements Serializable {

	private Vector<String> openJobs;
	private String reason;
	private String hostAddress;
	
	
	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	public Vector<String> getOpenJobs() {
		return openJobs;
	}
	
	public void setOpenJobs(Vector<String> openJobs) {
		this.openJobs = openJobs;
	}
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
}
