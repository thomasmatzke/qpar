package main.java.master;

import java.net.InetSocketAddress;
import java.util.Hashtable;

import main.java.messages.InformationMessage;
import main.java.slave.Tool;

/**
 * Holds slave informations. Schedules decomposition of a qbf
 * @author thomasm
 *
 */
public class Scheduler {
	
	private Hashtable<InetSocketAddress, InformationMessage> toolDirectory = new Hashtable<InetSocketAddress, InformationMessage>(); 
	
	/**
	 * Adds a slave server to the internal list
	 * @param addr Address and port of the slave
	 */
	public void addSlave(InetSocketAddress addr) {
		
	}
	
	public void setSchedulingHeuristic(SchedulingHeuristic s) {
		
	}
	
	public void startComputation() {
		
	}
}
