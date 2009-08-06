package main.java.messages;

import java.io.Serializable;

public class AbortMessage implements Serializable {

	private String jobId;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

}
