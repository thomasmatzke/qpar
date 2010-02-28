package main.java.slave.solver;

import main.java.logic.parser.SimpleNode;

import java.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import main.java.StreamGobbler;
import main.java.logic.TransmissionQbf;
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
	{
		logger.setLevel(Level.DEBUG);
	}
	public static final String toolId = "qpro";
	private Process qpro_process;
	private TransmissionQbf formula;
	private Master master;
	
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
		
	public static synchronized String toInputString(TransmissionQbf t) {
		Vector<Integer> eVars = new Vector<Integer>();
		Vector<Integer> aVars = new Vector<Integer>();
		Vector<Integer> vars = new Vector<Integer>();
		String traversedTree = "";

		vars = t.getVars();
		eVars = t.getEVars();
		aVars = t.getAVars();
			logger.info("WTF " + eVars + " " + aVars + " " + vars);
		t.checkQbf(); // just to make sure that there's really a tree
	
		// assign the truth values
		t.assignTruthValues();

		// reduce the tree
		t.reduceTree();
		
		// traverse the tree to get a string in qpro format
		traversedTree += "\nQBF\n" + (vars.size()+1) + "\nq\n" + "a ";
		for (int i=0; i < eVars.size(); i++)
			traversedTree += eVars.get(i) + " ";
		traversedTree += "\n" + "e ";
		for (int i=0; i < aVars.size(); i++)
			traversedTree += aVars.get(i) + " ";
		traversedTree += "\n";
		traversedTree += t.traverseTree();
		traversedTree += "/q\nQBF\n";	

		logger.info("traversing finished, tree:\n" + traversedTree);

		if(t.isValid()) {
			return traversedTree;
		}
		
		logger.info("sending fake formula");
		return "QBF\n4\nq\ne 2\na 3 4\nd\n 2 3 4\n\n/d\n/q\nQBF\n";
		
	}
}
