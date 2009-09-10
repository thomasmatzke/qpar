package main.java.logic;

public class TransmissionQbf {

	private static int idCounter = 0;
	private String id;
	
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
