package main.java.logic;

import java.util.Vector;
import java.util.ArrayList;
import main.java.logic.parser.SimpleNode;
import java.io.Serializable;
import main.java.QPar;

import main.java.master.MasterDaemon;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

// A TransmissionQbf contains a QBF as a tree as well as vectors and lists of
// all vars/exist-quantified vars/all-quantified vars/vars to assign true and
// vars to assign false. They are created in the Qbf class and sent to the
// slaves to be solved there.
public class TransmissionQbf implements Serializable {
	private SimpleNode root = null;
	private static int idCounter = 0;
	private String id;
	private Vector<Integer> eVars = new Vector<Integer>();
	private Vector<Integer> aVars = new Vector<Integer>();
	private Vector<Integer> vars = new Vector<Integer>();
	private ArrayList<Integer> trueVars = new ArrayList<Integer>();
	private ArrayList<Integer> falseVars = new ArrayList<Integer>();
	static Logger logger = Logger.getLogger(TransmissionQbf.class);

	/**
	 * constructor
	 */
	public TransmissionQbf() {
		logger.setLevel(QPar.logLevel);
	}

	/**
	 * setter for the trueVars ArrayList
	 * @param v the variable number to add
	 */
	public void addToTrueVars(int v) {
		trueVars.add(v);
	}

	/**
	 * setter for the falseVars ArrayList
	 * @param v the variable number to add
	 */
	public void addToFalseVars(int v) {
		falseVars.add(v);
	}

	/**
	 * getter for the trueVars ArrayList
	 * @return the ArrayList of true-assigned vars
	 */
	public ArrayList<Integer> getTrueVars() {
		return trueVars;
	}

	/**
	 * getter for the falseVars ArrayList
	 * @return the ArrayList of false-assigned vars
	 */
	public ArrayList<Integer> getFalseVars() {
		return falseVars;
	}

	/**
	 * debug method that logs the content of a transmissionQbf
	 */
	public void checkQbf() {
		logger.debug("checkQBF id: "+this.id);
//		logger.debug("checkQBF root node: " + root.getClass().getName() + " with " +
//		root.jjtGetNumChildren() + " children (should be 1. first one: " +
//		root.jjtGetChild(0).getClass().getName() + ")");
		logger.debug("checkQBF vars: " + this.vars);
		logger.debug("checkQBF eVars: " + this.eVars);
		logger.debug("checkQBF aVars: " + this.aVars);
		logger.debug("checkQBF trueVars: " + this.trueVars);
		logger.debug("checkQBF falseVars: " + this.falseVars);
	}

	/**
	 * getter for the eVars Vector
	 * @return the Vector of exist-quantified vars
	 */
	public Vector<Integer> getEVars() {
		return this.eVars;
	}

	/**
	 * getter for the aVars Vector
	 * @return the Vector of all-quantified vars
	 */
	public Vector<Integer> getAVars() {
		return this.aVars;
	}

	/**
	 * getter for the vars Vector
	 * @return the Vector of all variables that appear in a formula
	 */
	public Vector<Integer> getVars() {
		return this.vars;
	}

	/**
	 * setter for the exist-quantified vars
	 * @param v vector of exist-quantified vars
	 */
	public void setEVars(Vector<Integer> v) {
		this.eVars = v;
	}

	/**
	 * setter for the all-quantified vars
	 * @param v vector of all-quantified vars
	 */
	public void setAVars(Vector<Integer> v) {
		this.aVars = v;
	}

	/**
	 * setter for the vars
	 * @param v vector of all vars
	 */
	public void setVars(Vector<Integer> v) {
		this.vars = v;
	}

	/**
	 * assigns the transmissionQbf-specific truth values to the tree.
	 */
	public void assignTruthValues() {
		int i;
		for (i = 0; i < this.trueVars.size(); i++) {
			root.assignTruthValue(this.trueVars.get(i), true);
		}
		for (i = 0; i < this.falseVars.size(); i++) {
			root.assignTruthValue(this.falseVars.get(i), false);
		}
	}

	/**
	 * calls the reduce() method of the tree as long as there's something to reduce
	 */
	public void reduceTree() {
		boolean continueReducing = true;		

		while (continueReducing) {
			continueReducing = root.jjtGetChild(0).reduce();
		}
	}

	/**
	 * calls the traverse() method of the tree.
	 * @return The formula (without header and footer) in QPro format.
	 */
	public String traverseTree() {
		return root.jjtGetChild(0).traverse();
	}

	/**
	 * checks if the first node after the input node has a truth value assigned
	 * @return true if the first node after input has a truth value, false otherwise
	 */
	public boolean rootIsTruthNode() {
		if (root.jjtGetChild(0).getTruthValue().equals(""))
			return false;
		return true;
	}

	/**
	 * returns the truth value of the formulas root node
	 * @return a truth value
	 */
	public boolean rootGetTruthValue() {
		if (root.jjtGetChild(0).getTruthValue().equals("TRUE"))
			return true;
		return false;	
	}

	/**
	 * returns a vector of vars from aVars & eVars that don't appear in the tree
	 * @return a vector of orphaned quantified vars
	 */	
	public Vector<Integer> getOrphanedVars() {
		Vector<Integer> orphanedVars = new Vector<Integer>();
		Vector<Integer> quantifiedVars = new Vector<Integer>();

		quantifiedVars.addAll(aVars);
		quantifiedVars.addAll(eVars);
		
		for (int i = 0; i < quantifiedVars.size(); i++) {
			if (!root.findVar(quantifiedVars.get(i))) {
				orphanedVars.add(quantifiedVars.get(i));
			}
		}
		return orphanedVars;
	}

	// getter/setter, too self-explanatory for javadoc :)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SimpleNode getRootNode() {
		return root;
	}
	
	public void setRootNode(SimpleNode n) {
		this.root = n;
	}
}	


