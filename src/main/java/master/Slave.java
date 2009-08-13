package main.java.master;

import java.util.Vector;

public class Slave {
	private int cores;
	private Vector<String> toolIds;
	
	public int getCores() {
		return cores;
	}
	public void setCores(int cores) {
		this.cores = cores;
	}
	public Vector<String> getToolIds() {
		return toolIds;
	}
	public void setToolIds(Vector<String> toolIds) {
		this.toolIds = toolIds;
	}
	
	
}
