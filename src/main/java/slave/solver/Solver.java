package main.java.slave.solver;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import main.java.common.rmi.Result;
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
	
	public static HashMap<String, Solver> solvers = new HashMap<String, Solver>();
		
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
			solvers.put(tqbfId,this);
		} catch (RemoteException e) {
			logger.error("", e);
		}
	}

	public abstract void kill();

	public abstract void run();

	protected void returnWithError(String tqbfId, String jobId, Exception e) {
		Result r = new Result(tqbfId, jobId);
//		logger.error("Cant complete tqbf computation", e);
		r.type = Result.Type.ERROR;
		r.exception = e;
		try {
			this.tqbf.handleResult(r);
		} catch (RemoteException e1) {
			logger.error("", e);
		}
	}

	protected void returnWithSuccess(String tqbfId, String jobId,
			boolean result, long solverTime, long overheadTime) {
		Result r = new Result(tqbfId, jobId);

		if (result)
			r.type = Result.Type.TRUE;
		else
			r.type = Result.Type.FALSE;

		if (solverTime > 0)
			r.solverTime = solverTime;

		r.overheadTime = overheadTime;
		
		logger.info("Returning result for formula " + tqbfId + ": " + r.type);
		try {
			this.tqbf.handleResult(r);
		} catch (RemoteException e) {
			logger.error("", e);
		}

	}
	
	protected void returnWithSuccess(String tqbfId, String jobId, boolean result) {
		returnWithSuccess(tqbfId, jobId, result, 0, 0);
	}

}
