package main.java.rmi;

import java.io.Serializable;

public class Result implements Serializable {

	public enum Type { TRUE, FALSE, ERROR };
	public Type type = null;
	public Exception exception;
	public String errorMessage, tqbfId, jobId;
	
	public Result() {}
	public Result(String tqbfId, String jobId) {
		this.tqbfId = tqbfId;
		this.jobId = jobId;
	}
}
