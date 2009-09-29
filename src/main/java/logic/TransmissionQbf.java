package main.java.logic;

import java.io.Serializable;

public class TransmissionQbf implements Serializable {

	private static int idCounter = 0;
	private String id;
	private String status;		

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public static String allocateId() {
		idCounter++;
		return new Integer(idCounter).toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
