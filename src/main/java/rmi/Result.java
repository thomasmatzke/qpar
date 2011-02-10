package main.java.rmi;

import java.io.Serializable;

public class Result implements Serializable {

	public enum Type { TRUE, FALSE, ERROR };
	public Type type = null;
	public Exception exception;
	public String tqbfId, jobId;
	public long solverTime = 0;
	
	public Result(String tqbfId, String jobId) {
		this.tqbfId = tqbfId;
		this.jobId = jobId;
	}
	
	public Result(String tqbfId, String jobId, long solverTime) {
		this.tqbfId = tqbfId;
		this.jobId = jobId;
		this.solverTime = solverTime;
	}
}
