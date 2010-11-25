package main.java.slave.solver;

import main.java.logic.TransmissionQbf;

/**
 * Basic interface which all solvers have to implement. To appear in the list of
 * solvers, you have to add your solver in the SolverFactory
 * 
 * @author thomasm
 * 
 */
public interface Solver extends Runnable {

	// What was that for?
	// public static Hashtable<String, Solver> idToToolMap = new
	// Hashtable<String, Solver>();

	/**
	 * Setter to hand the qbf-formula to the solver
	 */
	public void setTransmissionQbf(TransmissionQbf formula);

	/**
	 * Called before the thread is run with Thread.start()
	 */
	public void prepare();

	/**
	 * Called when the slave receives a kill-message or an abort-message
	 */
	public void kill();

	public void run();

	public TransmissionQbf getTransmissionQbf();
	
	public Thread getThread();

}
