package main.java.messages;

import java.io.Serializable;

public class AbortMessage implements Serializable {

	private String qbfId;

	public String getQbfId() {
		return qbfId;
	}

	public void setQbfId(String qbfId) {
		this.qbfId = qbfId;
	}

}
