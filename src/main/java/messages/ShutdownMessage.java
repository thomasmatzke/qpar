package main.java.messages;

import java.io.Serializable;
import java.util.Vector;

/**
 * Sent by a slave to tell the master of its shutdown and the reason for it
 * @author thomasm
 *
 */
public class ShutdownMessage implements Serializable {
	
	private String reason;

	public ShutdownMessage(String reason){
		this.reason 		= reason;
	}
	
	public String getReason() {
		return reason;
	}

}
