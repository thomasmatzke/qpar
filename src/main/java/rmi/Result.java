package main.java.rmi;

import java.io.Serializable;

public class Result implements Serializable {

	public enum Type { TRUE, FALSE, ERROR };
	public Type type;
	public Exception exception;
	public String errorMessage, tqbfId, jobId;
	
}
