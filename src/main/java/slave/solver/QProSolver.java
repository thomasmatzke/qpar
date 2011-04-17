package main.java.slave.solver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.Date;

import main.java.common.rmi.InterpretationData;
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
		
		InterpretationData id = null;
		try {
			id = tqbf.getWorkUnit();
		} catch (RemoteException e2) {
			logger.error("", e2);
		}
		
		if(id.getRootNode() == null) {
			this.error();
			return;
		}
		
		ReducedInterpretation ri;
		try {			
			ri = new ReducedInterpretation(id);
		} catch (Exception e) {
			logger.error("Tqbf " + this.tqbfId, e);
			this.error();
			return;
		}
			
		this.overheadStoppedAt = new Date();
		
		if(ri.isTruthValue()) {
			logger.info("Formula " + this.tqbfId + " collapsed");
			this.terminate(ri.getTruthValue(), 0, this.overheadMillis());
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
			this.error();
			return;
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		executor.setStreamHandler(new PumpStreamHandler(output, null, input));
				
		try {
			synchronized(killMutex) {
				if (killed) {
					Solver.solvers.remove(this.tqbfId);
					return;
				}
				logger.info("Starting qpro process... (" + tqbfId + ")");
				this.run = true;
				executor.execute(command, resultHandler);
				this.qproProcessStartedAt = new Date();
			}
		} catch (ExecuteException e) {
			logger.error("", e);
			this.error();
			return;
		} catch (IOException e) {
			logger.error("", e);
			this.error();
			return;
		}
		
//		Slowdown for testing purposes	
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e2) {
//			e2.printStackTrace();
//		}
		
		while (!resultHandler.hasResult()) {
			try {
//				logger.info("waitsfor " + tqbf.getTimeout() * 1000 + " ms");
				resultHandler.waitFor(this.timeout * 1000);
				watchdog.destroyProcess();
				solvers.remove(this.tqbfId);
			} catch (InterruptedException e1) {
			}
			
		}
		this.qproProcessStoppedAt = new Date();
		logger.info("qpro process terminated... (" + tqbfId + ")");
				
		try {
			handleResult(output.toString("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			this.error();
			return;
		}
		Solver.solvers.remove(this.tqbfId);
	}

	private void handleResult(String readString) {
		if (killed) {
			this.error();
			return;
		}

		logger.info("qpro return string: " + readString);
				
		if(this.isTimedOut()) {
			// we timeouted
			this.timeout();
		} else if (readString.startsWith("1")) {
			// If qpro returns 1 the subformula is satisfiable
			this.terminate(true, this.solverMillis(), this.overheadMillis());
		} else if (readString.startsWith("0")) {
			// IF qpro returns 0 the subformula is unsatisfiable
			this.terminate(false, this.solverMillis(), this.overheadMillis());

		} else {
//			logger.error("Qpro Input of tqbf " + this.tqbfId + ": \n" + this.input);
			// anything else is an error
			String errorString = "Unexpected result from solver.\n"
					+ "	Return String: " + readString + "\n" + "	TQbfId:		 : "
					+ tqbfId + "\n";
			logger.error(errorString);
			this.error();
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
	
	public long solverMillis() {
		return this.qproProcessStoppedAt.getTime() - this.qproProcessStartedAt.getTime();
	}
	
	public long overheadMillis() {
		return this.overheadStoppedAt.getTime()	- this.overheadStartedAt.getTime();
	}
	
	public boolean isTimedOut() {
//		logger.info("SOLVERTIME: " + this.getSolvertime() + ", " + "TIMEOUT: " + this.timeout);
		return this.solverMillis() > this.timeout * 1000;
	}
}
