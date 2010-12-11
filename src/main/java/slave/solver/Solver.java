package main.java.slave.solver;

import main.java.logic.TransmissionQbf;

/**
 * Basic interface which all solvers have to implement. To appear in the list of
 * solvers, you have to add your solver in the SolverFactory
 * 
 * @author thomasm
 * 
 */
public abstract class Solver implements Runnable {

	protected TransmissionQbf formula;
	protected Thread thread;
	
	public abstract void kill();

	public abstract void run();

	public TransmissionQbf getTransmissionQbf() {
		return this.formula;
	}
	
	public void setTransmissionQbf(TransmissionQbf formula) {
		this.formula = formula;
	}
	
	public Thread getThread() {
		return this.thread;
	}

	public void setThread(Thread t) {
		this.thread = t;		
	}
}
