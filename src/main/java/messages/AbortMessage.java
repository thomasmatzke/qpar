package main.java.messages;

import java.io.Serializable;

public class AbortMessage implements Serializable {

	private int jobId;

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

}
