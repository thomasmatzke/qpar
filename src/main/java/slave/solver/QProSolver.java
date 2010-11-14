package main.java.slave.solver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Vector;

import main.java.QPar;
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
public class QProSolver implements Solver {

	static Logger logger = Logger.getLogger(QProSolver.class);

	public static final String toolId = "qpro";
	private Process qpro_process;
	private TransmissionQbf formula;
	private String inputString = null;
	private boolean killed = false;

	public QProSolver() {
		logger.setLevel(QPar.logLevel);
	}

	/**
	 * Kills the qpro-process
	 */
	public void kill() {
		this.killed = true;
		if (qpro_process != null)
			qpro_process.destroy();
	}

	public void prepare() {
	}

	public void setTransmissionQbf(TransmissionQbf formula) {
		this.formula = formula;
	}

	public TransmissionQbf getTransmissionQbf() {
		return formula;
	}

	public void run() {
		ProcessBuilder pb = new ProcessBuilder("qpro");
		Result r = new Result();
		r.tqbfId = formula.getId();
		r.jobId = formula.jobId;

		// Get the id from our formula so we can nullify it after qproization
		// -> for cleanup by garbage collector
		String tqbfId = this.formula.getId();
		try {
			logger.info("Starting qpro process...");
			qpro_process = pb.start();
			PrintWriter stdin = new PrintWriter(qpro_process.getOutputStream());
			logger.info("Generating qpro input...");
			this.inputString = toInputString(this.formula);
			this.formula = null;
			System.gc();
			if (inputString.equals("true")) {
				logger.info("Result for Subformula(" + tqbfId + ") was true");
				r.type = Result.Type.TRUE;
				Slave.master.returnResult(r);
				return;
			} else if (inputString.equals("false")) {
				r.type = Result.Type.FALSE;
				Slave.master.returnResult(r);
				logger.info("Result for Subformula(" + tqbfId + ") was false");
				return;
			}
			logger.info("Piping inputstring to qpro...");
			stdin.print(inputString);
			stdin.flush();
			InputStreamReader isr = new InputStreamReader(qpro_process
					.getInputStream());
			StringWriter writer = new StringWriter();
			IOUtils.copy(isr, writer);
			String readString = writer.toString();
			logger.info("Waiting for qpro...");
			qpro_process.waitFor();

			// If qpro returns 1 the subformula is satisfiable
			if (readString.startsWith("1")) {
				logger.info("Result for Subformula(" + tqbfId + ") was "
						+ new Boolean(true));
				r.type = Result.Type.TRUE;
				Slave.master.returnResult(r);

				// IF qpro returns 0 the subformula is unsatisfiable
			} else if (readString.startsWith("0")) {
				r.type = Result.Type.FALSE;
				Slave.master.returnResult(r);
				logger.info("Result for Subformula(" + tqbfId + ") was "
						+ new Boolean(false));

				// We have been killed by the master
			} else if (this.killed == true) {
				// do nothing

				// anything else is an error
			} else {
				if(!killed){
					logger.error("Unexpected result from solver.\n"
							+ "	Return String: " + readString + "\n"
							+ "	TQbfId:		 : " + tqbfId + "\n");
					if (QPar.logLevel == Level.DEBUG)
						logger.debug("Formulastring: \n" + this.inputString);
					r.type = Result.Type.ERROR;
					r.errorMessage = "Unexpected result from solver(" + readString
							+ "). Aborting Formula.";
					Slave.master.returnResult(r);
				}
			}
		} catch (IOException e) {
			if(!killed){
				logger.error("IO Error while getting result from solver: " + e);
				r.type = Result.Type.ERROR;
				r.exception = e;
				try {
					Slave.master.returnResult(r);
				} catch (RemoteException e1) {
					logger.error(e);
					System.exit(-1);
				}
			}
		} catch (InterruptedException e) {
			if(!killed){
				logger.error(e);
				r.type = Result.Type.ERROR;
				r.exception = e;
				try {
					Slave.master.returnResult(r);
				} catch (RemoteException e1) {
					logger.error(e);
					System.exit(-1);
				}
			}
		}
		Slave.threads.remove(tqbfId);
	}

	/**
	 * make a formula in qpro format from the transmission QBF
	 * 
	 * @param t
	 *            the QBF the slave gets from the master
	 * @return a string representation of the tree in QPRO format
	 */
	private static String toInputString(TransmissionQbf t) {
		Vector<Integer> eVars = new Vector<Integer>();
		Vector<Integer> aVars = new Vector<Integer>();
		// Vector<Integer> vars = new Vector<Integer>();
		Vector<Integer> orphanedVars = new Vector<Integer>();
		int i = 0;
		String traversedTree = "";

		// vars = t.getVars();
		eVars = t.getEVars();
		aVars = t.getAVars();

		// can be used to check if there's really some formula in t
		// t.checkQbf();

		// assign the truth values
		logger.debug("assigning truth values started");
		t.assignTruthValues();
		logger.debug("assigning truth values finished");

		// reduce the tree
		logger.debug("reducing started");
		t.reduceTree();
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
		orphanedVars = t.getOrphanedVars();
		for (i = 0; i < orphanedVars.size(); i++) {
			if (aVars.contains(orphanedVars.get(i)))
				aVars.remove(orphanedVars.get(i));
			if (eVars.contains(orphanedVars.get(i)))
				eVars.remove(orphanedVars.get(i));
		}

		logger.debug("check for orphaned quantified vars finished");

		// t.dump("DEBUG");
		// t.checkQbf();

		int maxVar = 0;
		for (i = 0; i < aVars.size(); i++) {
			if (aVars.get(i) >= maxVar)
				maxVar = aVars.get(i);
		}
		for (i = 0; i < eVars.size(); i++) {
			if (eVars.get(i) >= maxVar)
				maxVar = eVars.get(i);
		}

		logger.debug("max var: " + maxVar);

		// traverse the tree to get a string in qpro format
		logger.debug("traversing started");
		traversedTree += "QBF\n" + (maxVar) + "\n";
		;
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

		// logger.debug(traversedTree);

		return traversedTree;
		// return reindexVariables(traversedTree, vars);
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
}
