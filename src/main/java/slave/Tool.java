package main.java.slave;

import java.util.Hashtable;
import main.java.logic.TransmissionQbf;

public interface Tool extends Runnable{
	
	public static Hashtable<String, Class> idToToolMap = new Hashtable<String, Class>();
	
	public void setTransmissionQbf(TransmissionQbf formula);
	
	public void prepare();
	
	public boolean result() throws Exception;
	
	public void cleanup();
	
	public void kill();
	
}
