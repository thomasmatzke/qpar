package main.java.slave.solver;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import main.java.common.rmi.TQbfRemote;

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
	
	public static ConcurrentHashMap<String, Solver> solvers = new ConcurrentHashMap<String, Solver>();
		
	protected TQbfRemote tqbf;
	protected String tqbfId = null;
	protected String jobId = null;
	protected long timeout;
	
	volatile protected boolean killed = false;
	protected boolean run = true;
		
	public Solver(TQbfRemote tqbf) {
		try {
			this.tqbf = tqbf;
			this.tqbfId = tqbf.getId();
			this.jobId = tqbf.getJobId();
			this.timeout = tqbf.getTimeout();
			Solver.solvers.put(tqbfId,this);
		} catch (RemoteException e) {
			logger.error("", e);
		}
	}

	public abstract void kill();

	public abstract void run();

	protected void timeout() {
		try {
			this.tqbf.timeout();
		} catch (RemoteException e) {
			logger.error("RMI fail while sending timeout", e);
		}
		tearDown();
	}
	
	protected void terminate(boolean isSolvable, long solverMillis, long overheadMillis) {
		try {
			this.tqbf.setSolverMillis(solverMillis);
			this.tqbf.setOverheadMillis(solverMillis);
			this.tqbf.terminate(isSolvable);
		} catch (RemoteException e) {
			logger.error("RMI fail while sending result", e);
		}
		tearDown();
	}
	
	protected void error() {
		try {
			this.tqbf.error();
		} catch (RemoteException e) {
			logger.error("RMI fail while sending error", e);
		}
		tearDown();
	}
	
	protected void tearDown() {
		logger.info("Tearing down solver of tqbf " + this.tqbfId);
		Solver.solvers.remove(this.tqbfId);
	}
	
	public void finalize() {
		Solver.solvers.remove(this.tqbfId);
	}

}
