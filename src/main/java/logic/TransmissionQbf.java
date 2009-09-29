package main.java.logic;

import java.io.Serializable;

public class TransmissionQbf implements Serializable {

	private static int idCounter = 0;
	private int id;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
