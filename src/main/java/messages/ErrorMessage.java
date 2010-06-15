package main.java.messages;

import java.io.Serializable;

/**
 * Sent by the client if an error occurs which aborts formula computation
 * @author thomasm
 *
 */
public class ErrorMessage implements Serializable {
	private String message;
	private String tQbfId;
	
	//public ErrorMessage(){}
	public ErrorMessage(String tQbfId, String message) {
		this.tQbfId		= tQbfId;
		this.message 	= message;
	}
		
//	public void setMessage(String message) {
//		this.message = message;
//	}

//	public void setTQbfId(String tQbfId) {
//		this.tQbfId = tQbfId;
//	}
	
	public String getMessage() {
		return message;
	}
	
	public String getTQbfId() {
		return tQbfId;
	}
}
