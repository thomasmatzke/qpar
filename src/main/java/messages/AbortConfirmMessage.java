package main.java.messages;

import java.io.Serializable;

/**
 * 
 * @author thomasm
 *
 */
public class AbortConfirmMessage implements Serializable {
	private int jobId;

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

}
