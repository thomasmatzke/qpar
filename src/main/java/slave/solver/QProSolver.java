package main.java.slave.solver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.Date;

import main.java.common.rmi.TQbfRemote;
import main.java.slave.tree.QproRepresentation;
import main.java.slave.tree.ReducedInterpretation;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.log4j.Logger;

/**
 * This class encapsulates the qpro-solver
 * 
 * @author thomasm
 * 
 */
public class QProSolver extends Solver {

	static Logger logger = Logger.getLogger(QProSolver.class);

	public static final String toolId = "qpro";

	private Date qproProcessStartedAt = null;
	private Date qproProcessStoppedAt = null;
	
	private Date overheadStartedAt = null;
	private Date overheadStoppedAt = null;

	private ExecuteWatchdog watchdog = null;

	private volatile Object killMutex = new Object();
	
	String input = null;
	
	public QProSolver(TQbfRemote tqbf) {
		super(tqbf);
	}

	public void run() {
		this.overheadStartedAt = new Date();
		
//		this.inputString = toInputString(this.tqbf);
		ReducedInterpretation ri;
		try {
			ri = new ReducedInterpretation(tqbf.getWorkUnit());
		} catch (Exception e) {
			logger.error("Reducer fail", e);
			this.returnWithError(this.tqbfId, this.jobId, e);
			return;
		}
			
		this.overheadStoppedAt = new Date();
		
		if(ri.isTruthValue()) {
			logger.info("Formula " + this.tqbfId + " collapsed");
			returnWithSuccess(tqbfId, jobId, ri.getTruthValue());
			return;
		}

		Executor executor = new DefaultExecutor();

		watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
		executor.setWatchdog(watchdog);

		ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
		executor.setProcessDestroyer(processDestroyer);

		CommandLine command = new CommandLine("qpro");
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
//ri.getInterpretation().dump(this.tqbfId);
		QproRepresentation qproInput = new QproRepresentation(ri); 
		this.input = qproInput.getQproRepresentation();
//logger.info("qpro input for tqbf(" + this.tqbfId +")" + this.input);
		ByteArrayInputStream input;
		try {
			input = new ByteArrayInputStream(this.input.getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e1) {
			logger.error("", e1);
			returnWithError(tqbfId, jobId, e1);
			return;
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		executor.setStreamHandler(new PumpStreamHandler(output, null, input));
				
		try {
			synchronized(killMutex) {
				if (killed)
					return;
				logger.info("Starting qpro process... (" + tqbfId + ")");
				this.run = true;
				executor.execute(command, resultHandler);
				this.qproProcessStartedAt = new Date();
			}
		} catch (ExecuteException e) {
			logger.error("", e);
			returnWithError(tqbfId, jobId, e);
			return;
		} catch (IOException e) {
			logger.error("", e);
			returnWithError(tqbfId, jobId, e);
			return;
		}
		
//		Slowdown for testing purposes	
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
		
		while (!resultHandler.hasResult()) {
			try {
//				logger.info("waitsfor " + tqbf.getTimeout() * 1000 + " ms");
				resultHandler.waitFor(tqbf.getTimeout() * 1000);
				watchdog.destroyProcess();
				solvers.remove(this);
			} catch (InterruptedException e1) {
			} catch (RemoteException e) {
				watchdog.destroyProcess();
				logger.error("", e);
				return;
			}
			
		}
		this.qproProcessStoppedAt = new Date();
		logger.info("qpro process terminated... (" + tqbfId + ")");
				
		try {
			handleResult(output.toString("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			returnWithError(tqbfId, jobId, e);
			return;
		}
		
	}

	private void handleResult(String readString) {
		if (killed)
			return;

		logger.info("qpro return string: " + readString);
				
		if(this.isTimedOut()) {
			// we timeouted
			try {
				this.tqbf.timeout();
			} catch (RemoteException e) {
				logger.error("", e);
			}
		} if (readString.startsWith("1")) {
			// If qpro returns 1 the subformula is satisfiable
			returnWithSuccess(tqbfId, jobId, true, this.getSolvertime(), this.getOverheadtime());
		} else if (readString.startsWith("0")) {
			// IF qpro returns 0 the subformula is unsatisfiable
			returnWithSuccess(tqbfId, jobId, false, this.getSolvertime(), this.getOverheadtime());

		} else {
//			logger.error("Qpro Input of tqbf " + this.tqbfId + ": \n" + this.input);
			// anything else is an error
			String errorString = "Unexpected result from solver.\n"
					+ "	Return String: " + readString + "\n" + "	TQbfId:		 : "
					+ tqbfId + "\n";
			returnWithError(tqbfId, jobId, new Exception(errorString));
		}
	}

	@Override
	public void kill() {
		synchronized(killMutex) {
			if(this.run == false)
				return;
			if(watchdog != null)
				watchdog.destroyProcess();
			this.killed = true;
		}
	}
	
	public long getSolvertime() {
		return this.qproProcessStoppedAt.getTime() - this.qproProcessStartedAt.getTime();
	}
	
	public long getOverheadtime() {
		return this.overheadStoppedAt.getTime()	- this.overheadStartedAt.getTime();
	}
	
	public boolean isTimedOut() {
//		logger.info("SOLVERTIME: " + this.getSolvertime() + ", " + "TIMEOUT: " + this.timeout);
		return this.getSolvertime() > this.timeout * 1000;
	}
}
