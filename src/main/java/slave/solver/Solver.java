package main.java.slave.solver;

import java.rmi.RemoteException;

import main.java.master.TQbf;
import main.java.rmi.Result;
import main.java.rmi.TQbfRemote;

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
	protected TQbfRemote tqbf;
//	public Thread thread;
	protected ResultHandler handler = null;
	protected String tqbfId = null;
	protected String jobId = null;
	protected long timeout;
	
	volatile protected boolean killed = false;
	protected boolean run = true;
		
	public Solver(TQbfRemote tqbf2, ResultHandler handler) {
		this.handler = handler;
		try {
			this.tqbf = tqbf2;
			this.tqbfId = tqbf2.getId();
			this.jobId = tqbf2.getJobId();
			this.timeout = tqbf2.getTimeout();
		} catch (RemoteException e) {
			logger.error("", e);
		}
	}

	public abstract void kill();

	public abstract void run();

//	public TQbf getTransmissionQbf() {
//		return this.tqbf;
//	}

	protected void returnWithError(String tqbfId, String jobId, Exception e) {
		Result r = new Result(tqbfId, jobId);
//		logger.error("Cant complete tqbf computation", e);
		r.type = Result.Type.ERROR;
		r.exception = e;
		this.handler.handleResult(r);
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
		this.handler.handleResult(r);

	}
	
	protected void returnWithSuccess(String tqbfId, String jobId, boolean result) {
		returnWithSuccess(tqbfId, jobId, result, 0, 0);
	}

}
