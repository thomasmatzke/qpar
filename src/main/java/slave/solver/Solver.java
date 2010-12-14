package main.java.slave.solver;

import org.apache.log4j.Logger;

import main.java.logic.TransmissionQbf;
import main.java.slave.Slave;

/**
 * Basic interface which all solvers have to implement. To appear in the list of
 * solvers, you have to add your solver in the SolverFactory
 * 
 * @author thomasm
 * 
 */
public abstract class Solver implements Runnable {

	static Logger logger = Logger.getLogger(QProSolver.class);
	protected TransmissionQbf formula;
	protected Thread thread;
	protected Slave slave = null;
	
	public Solver(Slave slave) {
		this.slave = slave;
	}
	
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
