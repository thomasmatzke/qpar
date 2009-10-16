package main.java.slave.solver;

import java.util.Hashtable;

import main.java.logic.TransmissionQbf;

public interface Solver extends Runnable{
	
	public static Hashtable<String, Solver> idToToolMap = new Hashtable<String, Solver>();
	
	public void setTransmissionQbf(TransmissionQbf formula);
	
	public void prepare();
	
	public void cleanup();
	
	public void kill();
	
}
