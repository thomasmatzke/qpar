package main.java.messages;

import java.io.Serializable;

/**
 * 
 * @author thomasm
 *
 */
public class AbortConfirmMessage implements Serializable {
	private String jobId;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

}
