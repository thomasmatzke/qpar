package main.java.slave.solver;

import main.java.logic.TransmissionQbf;

import org.apache.log4j.Logger;

/**
 * Basic interface which all solvers have to implement. To appear in the list of
 * solvers, you have to add your solver in the SolverFactory
 * 
 * @author thomasm
 * 
 */
public abstract class Solver implements Runnable {

	static Logger logger = Logger.getLogger(QProSolver.class);
	protected TransmissionQbf tqbf;
	protected Thread thread;
	protected ResultHandler handler = null;
	
	public Solver(TransmissionQbf tqbf, ResultHandler handler) {
		this.handler = handler;
		this.tqbf = tqbf;
	}
	
	public abstract void kill();

	public abstract void run();

	public TransmissionQbf getTransmissionQbf() {
		return this.tqbf;
	}
		
	public Thread getThread() {
		return this.thread;
	}

	public void setThread(Thread t) {
		this.thread = t;		
	}
}
