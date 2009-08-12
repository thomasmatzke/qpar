package main.java.slave;

import main.java.logic.Qbf;

public interface Tool extends Runnable{
	
	public void setQbf(Qbf formula);
	
	public void prepare();
	
	public boolean result() throws Exception;
	
	public void cleanup();
	
	public void kill();
	
}
