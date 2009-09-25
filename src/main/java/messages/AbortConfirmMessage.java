package main.java.messages;

import java.io.Serializable;

/**
 * 
 * @author thomasm
 * 
 */
public class AbortConfirmMessage implements Serializable {
	private String tQbfId;

	public String getTqbfId() {
		return tQbfId;
	}

	public void setTqbfId(String tqbfId) {
		this.tQbfId = tqbfId;
	}

}
