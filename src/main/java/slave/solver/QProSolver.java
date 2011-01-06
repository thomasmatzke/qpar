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

	public static final String toolId = "qpro";
	private Process qpro_process;

	private String inputString = null;
	private boolean killed = false;

	public QProSolver(Slave slave) {
		super(slave);
	}

	/**
	 * Kills the qpro-process
	 */
	synchronized public void kill() {
		this.killed = true;
		if (qpro_process != null)
			qpro_process.destroy();

	}

	public void run() {
		String readString = null;
		Result r = new Result();
		r.tqbfId = this.formula.getId();
		r.jobId = formula.jobId;
		// Get the id from our formula so we can nullify it after
		// qproization
		// -> for cleanup by garbage collector
		String tqbfId = this.formula.getId();
		
		try {
			
			logger.info("Generating qpro input...");
			this.inputString = toInputString(this.formula);
			if (inputString.equals("true")) {
				logger.info("Result for Subformula(" + tqbfId
						+ ") was true. Formula collapsed to root-node");
				r.type = Result.Type.TRUE;
				this.slave.master.returnResult(r);
				return;
			} else if (inputString.equals("false")) {
				r.type = Result.Type.FALSE;
				this.slave.master.returnResult(r);
				logger.info("Result for Subformula(" + tqbfId
						+ ") was false. Formula collapsed to root-node");
				return;
			}

			this.formula = null;
			System.gc();
			
			
			InputStreamReader isr = null;
			BufferedReader br = null;
			OutputStreamWriter osw = null;
			
			synchronized (this) {
				synchronized(slave.threads) {
					if(this.killed) {
						this.slave.threads.remove(tqbfId);
						return;
					}
				}				
				logger.info("Starting qpro process...");
				ProcessBuilder pb = new ProcessBuilder("qpro");
				qpro_process = pb.start();
				isr = new InputStreamReader(qpro_process.getInputStream());
				br = new BufferedReader(isr);
				osw = new OutputStreamWriter(qpro_process.getOutputStream());
							
				logger.info("Piping inputstring to qpro...");
				osw.write(inputString);
				logger.info("Flushing stdin writer...");
				osw.flush();
			}
			
			logger.info("Waiting for qpro...");
			qpro_process.waitFor();
			logger.info("waitFor returned...");
						
			//StringWriter writer = new StringWriter();
			//IOUtils.copy(qpro_process.getInputStream(), writer);
//			logger.info("Stream copied...");
			//readString = writer.toString();
//			StreamGobbler omNomNom = new StreamGobbler(qpro_process.getInputStream());
//			new Thread(omNomNom).start();
//			readString = omNomNom.readString;
			
			String line = "";
			StringBuffer sb = new StringBuffer();

			for(int i = 0; i<2; i++) {
				line = br.readLine();
				logger.info("Read line: " + line);
				sb.append(line);
				sb.append(System.getProperty("line.separator")); // BufferedReader strips the EOL character so we add a new one!
			}
			readString = sb.toString();
			osw.close();
			isr.close();
			
			logger.info("Result aquired...");
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
				logger.info("Thread was killed...");
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
		} catch (IOException e) {
			if (!killed) {
				logger.error("IO Error while getting result from solver: " + e);
				r.type = Result.Type.ERROR;
				r.exception = e;
				try {
					this.slave.master.returnResult(r);
				} catch (RemoteException e1) {
					logger.error(e);
					System.exit(-1);
				}
			}
		} catch (InterruptedException e) {
			if (!killed) {
				logger.error(e);
				r.type = Result.Type.ERROR;
				r.exception = e;
				try {
					this.slave.master.returnResult(r);
				} catch (RemoteException e1) {
					logger.error(e);
					System.exit(-1);
				}
			}
		}
		if (qpro_process != null)
			qpro_process.destroy();
		this.slave.threads.remove(tqbfId);
	}

	/**
	 * make a formula in qpro format from the transmission QBF
	 * 
	 * @param t
	 *            the QBF the slave gets from the master
	 * @return a string representation of the tree in QPRO format
	 */
	private static String toInputString(TransmissionQbf t) {
		// Vector<Integer> eVars = new Vector<Integer>();
		// Vector<Integer> aVars = new Vector<Integer>();
		// Vector<Integer> vars = new Vector<Integer>();
		// Vector<Integer> orphanedVars = new Vector<Integer>();
		// int i = 0;
		String traversedTree = "";

		// vars = t.getVars();
		// eVars = t.getEVars();
		// aVars = t.getAVars();

		// can be used to check if there's really some formula in t
		// t.checkQbf();
		// t.dump(t.getId()+" PRE ");
		// assign the truth values
		logger.debug("assigning truth values started");
		t.assignTruthValues();
		logger.debug("assigning truth values finished");
		// t.dump(t.getId()+"POSTASSIGN ");
		// reduce the tree
		logger.debug("reducing started");
		// t.reduceTree();
		t.reduceFast();
		// t.dump(t.getId()+"POSTREDUCE ");
		logger.debug("reducing finished");

		// maybe reducing the tree left us with a truth node only, then we have
		// to give qpro a formula evaluating to that truth value
		logger.debug("check if reduced to a single truth value");
		if (t.rootIsTruthNode()) {
			if (t.rootGetTruthValue()) {
				// a formula evaluating to true
				// logger.debug("reduced to death, sending fake true formula");
				// return "QBF\n3\nq\ne 2\na 3\nd\n2\n3\n/d\n/q\nQBF\n";
				return "true";
			}
			// a formula evaluating to false
			// logger.debug("reduced to death, sending fake false formula");
			// return "QBF\n3\nq\ne 2\na 3\nc\n2\n3\n/c\n/q\nQBF\n";
			return "false";
		}
		logger.debug("check finnished");

		// check if there are still occurences of all- and exist-quantified vars
		// left in the tree after reducing. if not, remove them from aVars and
		// eVars
		logger.debug("check for orphaned quantified vars");
		// orphanedVars = t.getOrphanedVars();
		// for (i = 0; i < orphanedVars.size(); i++) {
		// if (t.getAVars().contains(orphanedVars.get(i)))
		// t.getAVars().remove(orphanedVars.get(i));
		// if (t.getEVars().contains(orphanedVars.get(i)))
		// t.getEVars().remove(orphanedVars.get(i));
		// }
		t.eliminateOrphanedVars();
		logger.debug("check for orphaned quantified vars finished");

		// t.dump("DEBUG");
		// t.checkQbf();
		// t.getRootNode().dump("NARF");
		logger.debug("max var: " + t.getMaxVar());

		// traverse the tree to get a string in qpro format
		logger.debug("traversing started");

		traversedTree += "QBF\n" + (t.getMaxVar()) + "\n";

		// traversedTree += "\nq";
		/*
		 * if(aVars.size() > 0) traversedTree += "\na ";for (i=0; i <
		 * aVars.size(); i++) traversedTree += aVars.get(i) + " ";
		 * if(eVars.size() > 0) traversedTree += "\ne ";for (i=0; i <
		 * eVars.size(); i++) traversedTree += eVars.get(i) + " ";traversedTree
		 * += "\n";
		 */
		traversedTree += t.traverseTree(); // <- actual traversion happens here
		/*
		 * traversedTree += "/q\n";
		 */
		traversedTree += "QBF\n";
		logger.debug("traversing finished");

		return traversedTree;
		// return reindexVariables(traversedTree, vars);
	}

	@Override
	public Thread getThread() {
		return thread;
	}

	// private static String reindexVariables(String s, Vector<Integer> vars) {
	// String replaced = s;
	// assert(new HashSet<Integer>(vars).size() == vars.size()); // uniqueness
	// check
	//
	// for(int i = 0; i < vars.size(); i++) {
	// String oldVariable = " " + vars.get(i).toString() + " ";
	// String newVariable = " " + (Integer.toString(i+2)) + " ";
	// logger.info(oldVariable +"->"+ newVariable);
	// replaced = replaced.replaceAll(oldVariable, newVariable);
	// }
	// return replaced;
	// }
	
	protected void finalize() throws Throwable {
		try {
			if (qpro_process != null)
				qpro_process.destroy();
		} finally {
			super.finalize();
		}
	}
}
