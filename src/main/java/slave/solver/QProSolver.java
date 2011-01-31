package main.java.slave.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Vector;

import main.java.QPar;
import main.java.StreamGobbler;
import main.java.logic.TransmissionQbf;
import main.java.master.Mailer;
import main.java.rmi.Result;
import main.java.slave.Slave;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class encapsulates the qpro-solver
 * 
 * @author thomasm
 * 
 */
public class QProSolver extends Solver {

	public static final String 	toolId = "qpro";
	private Process 			qproProcess;

	private String 				inputString = null;
	private boolean 			killed 		= false;
	private Result 				r 			= null;
	private String 				readString 	= null, tqbfId = null;
	private InputStreamReader 	isr 		= null;
	private BufferedReader 		br 			= null;
	private OutputStreamWriter 	osw 		= null;
	
	public QProSolver(Slave slave) {
		super(slave);
		
	}

	/**
	 * Kills the qpro-process
	 */
	synchronized public void kill() {
		this.killed = true;
		if (qproProcess != null)
			qproProcess.destroy();
	}

	public void run() {
				
		// Get the id from our formula so we can nullify it after
		// qproization
		// -> for cleanup by garbage collector
		tqbfId = this.formula.getId();
		r = new Result(tqbfId, formula.jobId);
						
		try {
			generateQproInput();
			this.formula = null;
			if(r.type != null)
				return;
			System.gc();
			try {
			startQpro();
			} catch (IOException e) {
				if (!killed) {
					logger.error("IO Error while getting result from solver: " + e);
					r.type = Result.Type.ERROR;
					r.exception = e;
					this.slave.master.returnResult(r);
				}
				return;
			}
			waitForQpro();
			if (qproProcess == null)
				return;
			if(!readQproResult()) {
				if (qproProcess != null)
					qproProcess.destroy();
				return;
			}				
			handleResult();
		} catch (RemoteException e) {
			// Comm fail...what to do now
			logger.error(e);
			if(QPar.isMailInfoComplete() && QPar.exceptionNotifierAddress != null)
				Mailer.send_mail(QPar.exceptionNotifierAddress, QPar.mailServer, QPar.mailUser, QPar.mailPass, "Exception Notification (QProSolver.main())", e.toString());
			slave.reconnect();
		}

		if (qproProcess != null)
			qproProcess.destroy();
		this.slave.threads.remove(tqbfId);
	}

	private void handleResult() throws RemoteException {
		// If qpro returns 1 the subformula is satisfiable
		if (readString.startsWith("1")) {
			logger.info("Result for Subformula(" + tqbfId + ") was "
					+ new Boolean(true));
			r.type = Result.Type.TRUE;
			this.slave.master.returnResult(r);

		// IF qpro returns 0 the subformula is unsatisfiable
		} else if (readString.startsWith("0")) {
			r.type = Result.Type.FALSE;
			this.slave.master.returnResult(r);
			logger.info("Result for Subformula(" + tqbfId + ") was "
					+ new Boolean(false));

		// We have been killed by the master
		} else if (this.killed == true) {
			
		// anything else is an error
		} else {
			logger.error("Unexpected result from solver.\n"
					+ "	Return String: " + readString + "\n"
					+ "	TQbfId:		 : " + tqbfId + "\n");
			if (QPar.logLevel == Level.DEBUG)
				logger.debug("Formulastring: \n" + this.inputString);
			r.type = Result.Type.ERROR;
			r.errorMessage = "Unexpected result from solver("
					+ readString + "). Aborting Formula.";
			this.slave.master.returnResult(r);
		}
	}

	private void waitForQpro() throws RemoteException {
		logger.info("Waiting for qpro...");
		try {
			if(qproProcess != null)
				qproProcess.waitFor();
		} catch (InterruptedException e) {
			if(!killed) {
				logger.error(e);
				if(QPar.isMailInfoComplete() && QPar.exceptionNotifierAddress != null)
					Mailer.send_mail(QPar.exceptionNotifierAddress, QPar.mailServer, QPar.mailUser, QPar.mailPass, "Exception Notification (QProSolver.main())", e.toString());
				r.type = Result.Type.ERROR;
				r.exception = e;
				this.slave.master.returnResult(r);
			}
		}
	}

	private void generateQproInput() throws RemoteException {
		logger.info("Generating qpro input...");
		this.inputString = toInputString(this.formula);
		if (inputString.equals("true")) {
			logger.info("Result for Subformula(" + tqbfId
					+ ") was true. Formula collapsed to root-node");
			r.type = Result.Type.TRUE;
			this.slave.master.returnResult(r);
		} else if (inputString.equals("false")) {
			r.type = Result.Type.FALSE;
			this.slave.master.returnResult(r);
			logger.info("Result for Subformula(" + tqbfId
					+ ") was false. Formula collapsed to root-node");
		}
	}

	private boolean readQproResult() throws RemoteException {
		try {
			String line = "";
			StringBuffer sb = new StringBuffer();

			for(int i = 0; i<2; i++) {
				line = br.readLine();
				sb.append(line);
				sb.append(System.getProperty("line.separator")); // BufferedReader strips the EOL character so we add a new one!
			}
			readString = sb.toString();
			osw.close();
			isr.close();
			return true;
		} catch (IOException e) {
			if (!killed) {
				logger.warn("IO Error while getting result from solver: " + e);
				r.type = Result.Type.ERROR;
				r.exception = e;
				this.slave.master.returnResult(r);
			}
			return false;
		}
		
	}

	private void startQpro() throws IOException {
		synchronized (this) {
			synchronized(slave.threads) {
				if(this.killed) {
					this.slave.threads.remove(tqbfId);
					return;
				}
			}				
			logger.info("Starting qpro process...");
			ProcessBuilder pb = new ProcessBuilder("qpro");
			qproProcess = pb.start();
			isr = new InputStreamReader(qproProcess.getInputStream());
			br = new BufferedReader(isr);
			osw = new OutputStreamWriter(qproProcess.getOutputStream());

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
System.out.println(traversedTree);		
		return traversedTree;
	}

	@Override
	public Thread getThread() {
		return thread;
	}
	
	protected void finalize() throws Throwable {
		try {
			if (qproProcess != null)
				qproProcess.destroy();
		} finally {
			super.finalize();
		}
	}
}
