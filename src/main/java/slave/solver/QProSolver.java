package main.java.slave.solver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Random;

import main.java.QPar;
import main.java.StreamGobbler;
import main.java.logic.Qbf;
import main.java.logic.TransmissionQbf;
import main.java.rmi.Result;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import sun.misc.Lock;

/**
 * This class encapsulates the qpro-solver
 * 
 * @author thomasm
 * 
 */
public class QProSolver extends Solver {

	static Logger logger = Logger.getLogger(QProSolver.class);
	
	public static final String toolId = "qpro";

	private String inputString = null;

	protected static Object globLock = new Object();
	
	private Date qproProcessStartedAt = null;
	private Date qproProcessStoppedAt = null;

	public QProSolver(TransmissionQbf tqbf, ResultHandler handler) {
		super(tqbf, handler);
	}

	public void run() {

		// generateQproInput();

		this.inputString = toInputString(this.tqbf);
		if (inputString.equals("true")) {
			logger.info("Formula collapsed");
			returnWithSuccess(tqbfId, jobId, true);
			return;
		} else if (inputString.equals("false")) {
			logger.info("Formula collapsed");
			returnWithSuccess(tqbfId, jobId, false);
			return;
		}

		this.tqbf = null;
		System.gc();
		
		logger.info("Starting qpro process...");
		ProcessBuilder pb = new ProcessBuilder("qpro");
		pb.redirectErrorStream(true);
		try {
			solverProcess = pb.start();
		} catch (IOException e) {
			// Try again because of windows suckiness (but wait a bit)
			logger.error("Windows suckiness. Trying again to start qpro...", e);
			try {
				Thread.sleep(new Random().nextInt(100));
			} catch (InterruptedException e1) {
				logger.error("", e1);
			}
			killSolverProcess();
			pb = new ProcessBuilder("qpro");
			pb.redirectErrorStream(true);
			try {
				solverProcess = pb.start();
			} catch (IOException e1) {
				// second fail.... fuck you!
				if (!killed) {
					logger.error("Starting of qpro failed twice. Giving up", e);
					returnWithError(tqbfId, jobId, e);
				}
				return;
			}
		}
					
		logger.info("Starting streamgobblers");
		StreamGobbler stdoutStreamGobbler = new StreamGobbler(solverProcess.getInputStream());
//        StreamGobbler stderrStreamGobbler = new StreamGobbler(solverProcess.getErrorStream());
        Thread stdoutThread = new Thread(stdoutStreamGobbler);
//        Thread stderrThread = new Thread(stderrStreamGobbler);
		stdoutThread.start();
//		stderrThread.start();
        		
		logger.info("Piping inputstring to qpro...");
		this.qproProcessStartedAt = new Date();
        if(inputString != null) {
        	OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(solverProcess.getOutputStream()));
            try {
				writer.write(inputString);
				writer.flush();
	            writer.close();
			} catch (IOException e) {
				if(killed) {
					return;
				}
				logger.error("Something happened while piping input to qpro", e);
				returnWithError(tqbfId, jobId, e);
				return;
			}
            
        }
     
        
        logger.info("Waiting for gobblers to finish");
        try {
        	stdoutThread.join();
//        	stderrThread.join();
		} catch (InterruptedException e) {
			logger.error("Interrupted while waiting for streamgobbler", e);
			returnWithError(tqbfId, jobId, e);
			return;
		}
		logger.info("Gobblers returned");
		
		// Check for process termination. if process terminated, but readers are not done, the stream hangs
        // Fucking java bugs....
        try { solverProcess.waitFor(); } catch (InterruptedException e1) {}
		
        try {
			solverProcess.getErrorStream().close();	solverProcess.getInputStream().close();	solverProcess.getOutputStream().close();
		} catch (IOException e) {
			logger.error("", e);
		}
				
		this.qproProcessStoppedAt = new Date();
		
		handleResult(stdoutStreamGobbler.result);
	
	}

	private void handleResult(String readString) {
		long solverTime = this.qproProcessStoppedAt.getTime()
				- this.qproProcessStartedAt.getTime();
		// If qpro returns 1 the subformula is satisfiable
		if (readString.startsWith("1")) {
			returnWithSuccess(tqbfId, jobId, true, solverTime);

			// IF qpro returns 0 the subformula is unsatisfiable
		} else if (readString.startsWith("0")) {
			returnWithSuccess(tqbfId, jobId, false, solverTime);

			// We have been killed by the master
		} else if (this.killed == true) {

			// anything else is an error
		} else {
			String errorString = "Unexpected result from solver.\n"
					+ "	Return String: " + readString + "\n" + "	TQbfId:		 : "
					+ tqbfId + "\n";
			returnWithError(tqbfId, jobId, new Exception(errorString));
		}
	}





	/**
	 * make a formula in qpro format from the transmission QBF
	 * 
	 * @param t
	 *            the QBF the slave gets from the master
	 * @return a string representation of the tree in QPRO format
	 */
	private static String toInputString(TransmissionQbf t) {
		String traversedTree = "";
		t.assignTruthValues();
		t.reduceFast();

		if (t.rootIsTruthNode()) {
			if (t.rootGetTruthValue()) {
				return "true";
			}
			return "false";
		}

		// check if there are still occurences of all- and exist-quantified vars
		// left in the tree after reducing. if not, remove them from aVars and
		// eVars
		t.eliminateOrphanedVars();

		// traverse the tree to get a string in qpro format
		traversedTree += "QBF\n" + (t.getMaxVar()) + "\n";
		traversedTree += t.traverseTree(); // <- actual traversion happens here
		traversedTree += "QBF\n";
		logger.debug("traversing finished");
		return traversedTree;
	}

	@Override
	public Thread getThread() {
		return thread;
	}

	protected void finalize() throws Throwable {
		try {
			killSolverProcess();
		} finally {
			super.finalize();
		}
	}
}
