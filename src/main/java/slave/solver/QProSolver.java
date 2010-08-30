package main.java.slave.solver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

import main.java.QPar;
import main.java.logic.TransmissionQbf;
import main.java.slave.Master;
import main.java.slave.SlaveDaemon;

import org.apache.commons.io.IOUtils;
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
	private String inputString = null;
	private boolean killed = false;
	
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
		this.killed = true;
		if(qpro_process != null)
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
			this.inputString = toInputString(this.formula);
			logger.debug("QPRO INPUT FORMULA. TQBFID: " + this.formula.getId() + "\n" + inputString);
			stdin.print(inputString);
			stdin.flush();
			InputStreamReader isr = new InputStreamReader(qpro_process.getInputStream());
			StringWriter writer = new StringWriter();
			IOUtils.copy(isr, writer);
			String readString = writer.toString();
			qpro_process.waitFor();
			// If qpro returns 1 the subformula is satisfiable
			if(readString.startsWith("1")) {
				logger.info("Result for Subformula(" + this.formula.getId() + ") was " + new Boolean(true) );
				master.sendResultMessage(formula.getId(), new Boolean(true));				

			// IF qpro returns 0 the subformula is unsatisfiable
			} else if (readString.startsWith("0")) {
				master.sendResultMessage(formula.getId(), new Boolean(false));
				logger.info("Result for Subformula(" + this.formula.getId() + ") was " + new Boolean(false) );
			
			// We have been killed by the master
			} else if (this.killed == true) {
				master.sendFormulaAbortedMessage(formula.getId());
				
			// anything else is an error 
			} else {
				logger.error(	"Unexpected result from solver.\n" +
								"	Return String: " + readString + "\n" +
								"	TQbfId:		 : " + formula.getId() + "\n" +
								"	Formulastring: " + this.inputString);
				master.sendErrorMessage(formula.getId(), "Unexpected result from solver(" + readString + "). Aborting Formula.");
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
		Vector<Integer> orphanedVars = new Vector<Integer>();
		int i = 0;
		String traversedTree = "";
		
		vars = t.getVars();
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
				logger.debug("reduced to death, sending fake true formula");
				return "QBF\n3\nq\ne 2\na 3\nd\n2\n3\n/d\n/q\nQBF\n";
			}
			// a formula evaluating to false
				logger.debug("reduced to death, sending fake false formula");
			return "QBF\n3\nq\ne 2\na 3\nc\n2\n3\n/c\n/q\nQBF\n";
		}
		logger.debug("check finnished");
		
		// check if there are still occurences of all- and exist-quantified vars
		// left in the tree after reducing. if not, remove them from aVars and eVars
		logger.debug("check for orphaned quantified vars");
		orphanedVars = t.getOrphanedVars();
		for (i = 0; i < orphanedVars.size(); i++) {
			if (aVars.contains(orphanedVars.get(i)))
				aVars.remove(orphanedVars.get(i));
			if (eVars.contains(orphanedVars.get(i)))
				eVars.remove(orphanedVars.get(i));
		}
		
		vars = new Vector<Integer>();
		vars.addAll(aVars);
		vars.addAll(eVars);
		
		logger.debug("check for orphaned quantified vars finished");		

		// t.dump("DEBUG");
		// t.checkQbf();

		// traverse the tree to get a string in qpro format
		logger.debug("traversing started");
		traversedTree += "\nQBF\n" + (vars.size()+1) + "\n" +
				"q";
		if(aVars.size() > 0)
			traversedTree += "\na ";
		for (i=0; i < aVars.size(); i++)
			traversedTree += aVars.get(i) + " ";
		if(eVars.size() > 0)
			traversedTree += "\ne ";
		for (i=0; i < eVars.size(); i++)
			traversedTree += eVars.get(i) + " ";
		traversedTree += "\n";
		traversedTree += t.traverseTree(); // <- actual traversion happens here
		traversedTree += "/q\nQBF\n";	
		logger.debug("traversing finished");

		// logger.debug(traversedTree);

		return traversedTree;
	}	
}
