package main.java.logic;

import java.util.Vector;
import java.util.ArrayList;
import main.java.logic.parser.SimpleNode;
import java.io.Serializable;

import main.java.master.MasterDaemon;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

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
	{
		logger.setLevel(Level.DEBUG);
	}

	public boolean isValid() {
		return (root.findNodes(eVars) || root.findNodes(aVars));
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
		logger.info("checkQBF id: "+this.id);
//		logger.info("checkQBF root node: " + root.getClass().getName() + " with " +
//		root.jjtGetNumChildren() + " children (should be 1. first one: " +
//		root.jjtGetChild(0).getClass().getName() + ")");
		logger.info("checkQBF vars: " + this.vars);
		logger.info("checkQBF eVars: " + this.eVars);
		logger.info("checkQBF aVars: " + this.aVars);
		logger.info("checkQBF trueVars: " + this.trueVars);
		logger.info("checkQBF falseVars: " + this.falseVars);
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
		logger.info("entering TransmissionQbf.assignTruthValues()" + this.trueVars.size() + this.falseVars.size());
		int i;
		for (i = 0; i < this.trueVars.size(); i++) {
			logger.info("assigning true to " + this.trueVars.get(i));
			root.assignTruthValue(this.trueVars.get(i), true);
		}
		for (i = 0; i < this.falseVars.size(); i++) {
			logger.info("assigning false to " + this.falseVars.get(i));
			root.assignTruthValue(this.falseVars.get(i), false);
		}
		logger.info("exiting TransmissionQbf.assignTruthValues()");
	}

	/**
	 * calls the reduce() method of the tree as long as there's something to reduce
	 */
	public void reduceTree() {
		boolean continueReducing = true;
		
		logger.info("entering TransmissionQbf.reduceTree");
		root.dump("");

		while (continueReducing) {
			logger.info("reducing tree step begin");		
			continueReducing = root.jjtGetChild(0).reduce();	
			logger.info("reducing tree step end");		
		}
		root.dump("");
		logger.info("exiting TransmissionQbf.reduceTree");
	}

	/**
	 * calls the traverse() method of the tree.
	 * @return The formula (without header and footer) in QPro format.
	 */
	public String traverseTree() {
		logger.info("entering TransmissionQbf.traverseTree");
		return root.jjtGetChild(0).traverse();
	}

//	public static String allocateId() {
//		idCounter++;
//		return new Integer(idCounter).toString();
//	}

	// getter/setter
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
