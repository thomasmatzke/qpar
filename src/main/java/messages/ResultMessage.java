package main.java.messages;

import java.io.Serializable;

public class ResultMessage implements Serializable {
	private int jobId;
	private boolean result;
	
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public boolean isResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
}
