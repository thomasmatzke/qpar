package main.java.messages;

import java.io.Serializable;

/**
 * Tells the master, that the computation of a formula on a slave has successfully
 * terminated
 * @author thomasm
 * 
 */
public class FormulaAbortedMessage implements Serializable {
	private String tQbfId;

	public FormulaAbortedMessage(String tqbfId) {
		this.tQbfId = tqbfId;
	}
	
	public String getTqbfId() {
		return tQbfId;
	}

	

}
