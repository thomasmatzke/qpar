package main.java.messages;

import java.io.Serializable;

public class ShutdownMessage implements Serializable {

	public int openJobs[];
	public String reason;	
}
