package main.java.master;

import main.java.logic.Qbf;

/**
 * Controls "who gets what"
 * @author thomasm
 *
 */
public interface SchedulingHeuristic {
	
	/**
	 * Loads the qbf that has to be solved
	 * @param q
	 */
	public void setQbf(Qbf q);
	
	public void setScheduler(Scheduler s);
}
