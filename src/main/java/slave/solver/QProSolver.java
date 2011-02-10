package main.java.slave.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Random;

import main.java.QPar;
import main.java.logic.TransmissionQbf;
import main.java.rmi.Result;

import org.apache.log4j.Level;

/**
 * This class encapsulates the qpro-solver
 * 
 * @author thomasm
 * 
 */
public class QProSolver extends Solver {

	public static final String toolId = "qpro";
//	private Process qproProcess;

	private String inputString = null;
	private Result r = null;
	private String readString = null;
	private InputStreamReader isr = null;
	private BufferedReader br = null;
	private OutputStreamWriter osw = null;

	private Date qproProcessStartedAt = null;
	private Date qproProcessStoppedAt = null;

	public QProSolver(TransmissionQbf tqbf, ResultHandler handler) {
		super(tqbf, handler);
	}

//	/**
//	 * Kills the qpro-process
//	 */
//	synchronized public void kill() {
//		this.killed = true;
//		if (qproProcess != null)
//			qproProcess.destroy();
//	}

	public void run() {
		
//		generateQproInput();
		
		this.inputString = toInputString(this.tqbf);
		if(inputString.equals("true")) {
			returnWithSuccess(tqbfId, jobId, true);
			return;
		} else if(inputString.equals("false")) {
			returnWithSuccess(tqbfId, jobId, false);
			return;
		}
		
		this.tqbf = null;
		System.gc();
		
		try {
			startQpro();
		} catch (IOException e) {
			if (!killed) {
				logger.error("IO Error while getting result from solver", e);
				returnWithError(tqbfId, jobId, e);
			}
			return;
		}
		
		waitForQpro();		
		
		if (solverProcess == null) {
			returnWithError(tqbfId, jobId, new Exception("No qpro process"));
			return;
		}
		
		try {
			readQproResult();
		} catch(IOException e) {
			if(!killed){
				// IPC fucked up...
				returnWithError(tqbfId, jobId, e);
			}
			// Make sure its dead...
			killSolverProcess();
			return;
		}

		handleResult();

		killSolverProcess();
	}

	private void handleResult() {
		long solverTime = this.qproProcessStoppedAt.getTime() - this.qproProcessStartedAt.getTime();
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
			String errorString = 
				"Unexpected result from solver.\n" + 
				"	Return String: " + readString + "\n" +
				"	TQbfId:		 : " + tqbfId + "\n";
			returnWithError(tqbfId, jobId, new Exception(errorString));
		}
	}

	private void waitForQpro() {
		logger.info("Waiting for qpro...");
		try {
			if (solverProcess != null) {
				solverProcess.waitFor();
				this.qproProcessStoppedAt = new Date();
			}
		} catch (InterruptedException e) {
			if (!killed) {
				QPar.sendExceptionMail(e);
				returnWithError(tqbfId, jobId, e);
			}
		}
		
	}

//	private void generateQproInput() {
//		logger.debug("Generating qpro input...");
//		this.inputString = toInputString(this.tqbf);
//		if (inputString.equals("true")) {
//			logger.info("Result for Subformula(" + tqbfId
//					+ ") was true. Formula collapsed to root-node");
//			r.type = Result.Type.TRUE;
//			this.handler.handleResult(r);
//		} else if (inputString.equals("false")) {
//			r.type = Result.Type.FALSE;
//			this.handler.handleResult(r);
//			logger.info("Result for Subformula(" + tqbfId
//					+ ") was false. Formula collapsed to root-node");
//		}
//	}

	private void readQproResult() throws IOException {
//		try {
			String line = "";
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < 2; i++) {
				line = br.readLine();
				sb.append(line);
				sb.append(System.getProperty("line.separator")); // BufferedReader
																	// strips
																	// the EOL
																	// character
																	// so we add
																	// a new
																	// one!
			}
			readString = sb.toString();
			osw.close();
			isr.close();
//			return true;
//		} catch (IOException e) {
//			if (!killed) {
//				logger.warn("IO Error while getting result from solver: " + e);
//				r.type = Result.Type.ERROR;
//				r.exception = e;
//				this.handler.handleResult(r);
//			}
//			return false;
//		}

	}

	private void startQpro() throws IOException {
		synchronized (this) {
			logger.info("Starting qpro process...");
			ProcessBuilder pb = new ProcessBuilder("qpro");
			
			try {
			solverProcess = pb.start();
			}catch(IOException e) {
				// Try again because of windows suckiness (but wait a bit)
				try {
					Thread.sleep(new Random().nextInt(100));
				} catch (InterruptedException e1) {}
				if(solverProcess != null)
					solverProcess.destroy();
				pb = new ProcessBuilder("qpro");
				solverProcess = pb.start();
			}			
			this.qproProcessStartedAt = new Date();
			isr = new InputStreamReader(solverProcess.getInputStream());
			br = new BufferedReader(isr);
			osw = new OutputStreamWriter(solverProcess.getOutputStream());
			logger.info("Piping inputstring to qpro...");
			osw.write(inputString);
			osw.flush();
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
			if (solverProcess != null)
				solverProcess.destroy();
		} finally {
			super.finalize();
		}
	}
}
