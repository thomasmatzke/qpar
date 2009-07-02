package main.java.master;

import java.net.InetSocketAddress;

/**
 * Holds slave informations. Schedules decomposition of a qbf
 * @author thomasm
 *
 */
public interface Scheduler {
	
	/**
	 * Adds a slave server to the internal list
	 * @param addr Address and port of the slave
	 */
	public void addSlave(InetSocketAddress addr);
	
	public void setSchedulingHeuristic(SchedulingHeuristic s);
	
	public void startComputation();
}
