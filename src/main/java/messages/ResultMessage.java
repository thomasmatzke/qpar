package main.java.messages;

import java.io.Serializable;

public class ResultMessage implements Serializable {
	private String jobId;
	private boolean result;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
}
