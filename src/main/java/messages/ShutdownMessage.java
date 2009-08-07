package main.java.messages;

import java.io.Serializable;
import java.util.Vector;

public class ShutdownMessage implements Serializable {

	public Vector<String> openJobs;
	public String reason;
	
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
