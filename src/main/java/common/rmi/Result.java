package main.java.common.rmi;

import java.io.Serializable;

public class Result implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3176636668860263799L;

	public enum Type { TRUE, FALSE, ERROR };
	public Type type = null;
	public Exception exception;
	public String tqbfId, jobId;
	public long solverTime = 0;
	public long overheadTime = 0;
	
	public Result(String tqbfId, String jobId) {
		this.tqbfId = tqbfId;
		this.jobId = jobId;
	}
	
	public Result(String tqbfId, String jobId, long solverTime) {
		this.tqbfId = tqbfId;
		this.jobId = jobId;
		this.solverTime = solverTime;
	}
	
	public boolean getResult() {
		if(this.type == Type.TRUE) {
			return true;
		} else if(this.type == Type.FALSE) {
			return false;
		} else {
			throw new IllegalStateException("Result is neither true or false");
		}
	}
}
