package main.java.messages;

import java.io.Serializable;

/**
 * Aborts the computation of a qbf on a slave 
 * @author thomasm
 *
 */
public class AbortMessage implements Serializable {

	/**
	 * Id of the sub-qbf
	 */
	private String qbfId;

	public AbortMessage(String qbfId) {
		this.qbfId = qbfId;
	}
	
	public String getQbfId() {
		return qbfId;
	}

	

}
