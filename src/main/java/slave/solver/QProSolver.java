package main.java.slave.solver;

import main.java.logic.parser.SimpleNode;

import java.util.Vector;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import main.java.QPar;
import main.java.StreamGobbler;
import main.java.logic.TransmissionQbf;
import main.java.master.MasterDaemon;
import main.java.slave.Master;
import main.java.slave.SlaveDaemon;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class encapsulates the qpro-solver
 * @author thomasm
 *
 */
public class QProSolver implements Solver {

	static Logger logger = Logger.getLogger(QProSolver.class);
	
	public static final String toolId = "qpro";
	private Process qpro_process;
	private TransmissionQbf formula;
	private Master master;
	
	public QProSolver() {
		logger.setLevel(QPar.logLevel);
	}
	
	public Master getMaster() {
		return master;
	}

	/**
	 * Sets a master instance to send a result back to it
	 */
	public void setMaster(Master master) {
		this.master = master;
	}
	
	/**
	 * Kills the qpro-process
	 */
	public void kill() {
		qpro_process.destroy();
	}

	
	public void prepare() {}
	
	public void setTransmissionQbf(TransmissionQbf formula) {
		this.formula = formula;
	}

	
	public void run() {
		prepare();
		ProcessBuilder pb = new ProcessBuilder("qpro");
		try {
			qpro_process = pb.start();
			logger.debug("qpro started");
			PrintWriter stdin = new PrintWriter(qpro_process.getOutputStream());
			stdin.print(toInputString(this.formula));
			stdin.flush();
			//StreamGobbler gobbler = new StreamGobbler(qpro_process.getInputStream());
			//gobbler.start();
			InputStreamReader isr = new InputStreamReader(qpro_process.getInputStream());
			StringWriter writer = new StringWriter();
			IOUtils.copy(isr, writer);
			String readString = writer.toString();
			int return_val = qpro_process.waitFor();
			// If qpro returns 1 the subformula is satisfiable
			if(readString.startsWith("1")) {
				master.sendResultMessage(formula.getId(), new Boolean(true));
				logger.info("Result for Subformula(" + this.formula.getId() + ") was " + new Boolean(true) );

			// IF qpro returns 0 the subformula is unsatisfiable
			} else if (readString.startsWith("0")) {
				master.sendResultMessage(formula.getId(), new Boolean(false));
				logger.info("Result for Subformula(" + this.formula.getId() + ") was " + new Boolean(false) );
			// anything else is an error
			} else {
				logger.error("Got non-expected result from solver(" + readString + "). Aborting Formula.");
				master.sendErrorMessage(formula.getId(), "Got non-expected result from solver(" + readString + "). Aborting Formula.");
			}
		} catch (IOException e) {
			logger.error("IO Error while getting result from solver: " + e);
			master.sendErrorMessage(formula.getId(), e.toString());
		} catch (InterruptedException e) {
			logger.error(e);
		}
		SlaveDaemon.getThreads().remove(formula.getId());
	}
	
	/**
	 * make a formula in qpro format from the transmission QBF
	 * @param t the QBF	the slave gets from the master
	 * @return a string representation of the tree in QPRO format
	 */
	public static String toInputString(TransmissionQbf t) {
		Vector<Integer> eVars = new Vector<Integer>();
		Vector<Integer> aVars = new Vector<Integer>();
		Vector<Integer> vars = new Vector<Integer>();

		String traversedTree = "";
		
		vars = t.getVars();
		eVars = t.getEVars();
		aVars = t.getAVars();

		// just to make sure that there's really a tree TODO delete one day
		t.checkQbf();

		// assign the truth values
		logger.debug("assigning truth values started");
		t.assignTruthValues();
		logger.debug("assigning truth values finished");

		// reduce the tree
		logger.debug("reducing started");
		t.reduceTree();
		logger.debug("reducing finished");
		
		// traverse the tree to get a string in qpro format
		logger.debug("traversing started");
		traversedTree += "\nQBF\n" + (vars.size()+1) + "\nq\n" + "a ";
		for (int i=0; i < eVars.size(); i++)
			traversedTree += eVars.get(i) + " ";
		traversedTree += "\n" + "e ";
		for (int i=0; i < aVars.size(); i++)
			traversedTree += aVars.get(i) + " ";
		traversedTree += "\n";
		traversedTree += t.traverseTree(); // <- actual traversion happens here
		traversedTree += "/q\nQBF\n";	
//		logger.debug("traversing finished, tree: " + traversedTree);

		// check if quantified vars still occur in formula since qpro is no
		// friend of such formulas
		logger.debug("check if traversed formula is solvable by qpro");
		if(t.isValid()) {
			logger.debug("check ok, returning formula to qpro");
			logger.debug(traversedTree);
			return traversedTree;
		}
		
		logger.debug("check failed, sending fake formula to avoid qpro crash");
		return "QBF\n4\nq\ne 2\na 3 4\nd\n 2 3 4\n\n/d\n/q\nQBF\n";		
	}	
}
