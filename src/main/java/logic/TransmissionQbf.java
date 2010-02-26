package main.java.logic;

import java.util.Vector;
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
	private Vector<Integer> trueVars = new Vector<Integer>();
	private Vector<Integer> falseVars = new Vector<Integer>();
	static Logger logger = Logger.getLogger(TransmissionQbf.class);
	{
		logger.setLevel(Level.DEBUG);
	}

	// checks if there's a root node with a child
	public void checkQbf() {
		logger.info("checkQBF root node is: " + root.getClass().getName() + " with " +
		root.jjtGetNumChildren() + " direct children (should be 1. first one: " +
		root.jjtGetChild(0).getClass().getName() + ")");
		logger.info("checkQBF vars: " + vars + eVars + aVars + trueVars + falseVars);
	}

	public void setTrueVars( Vector<Integer> v) {
		this.trueVars = v;
		logger.info("set true vars in transmissionqbf to: " + v + " " + trueVars);
	}

	public void setFalseVars( Vector<Integer> v) {
		this.falseVars = v;
		logger.info("set false vars in transmissionqbf to: " + v + " " + falseVars);
	}

	public Vector<Integer> getTrueVars() {
		return this.trueVars;
	}

	public Vector<Integer> getFalseVars() {
		return this.falseVars;
	}

	public Vector<Integer> getEVars() {
		logger.info("request for eVars: " + eVars);
		return this.eVars;
	}

	public Vector<Integer> getAVars() {
		return this.aVars;
	}

	public Vector<Integer> getVars() {
		return this.vars;
	}

	public void setEVars(Vector<Integer> v) {
		this.eVars = v;
		logger.info("set eVars in transmissionqbf to: " + v + " " + eVars);
	}

	public void setAVars(Vector<Integer> v) {
		this.aVars = v;
		logger.info("set aVars in transmissionqbf to: " + v + " " + aVars);
	}

	public void setVars(Vector<Integer> v) {
		this.vars = v;
		logger.info("set vars in transmissionqbf to: " + v + " " + vars);
	}

	public void assignTruthValues() {
		logger.info("entering TransmissionQbf.assignTruthValues()" + this.trueVars.size() + this.falseVars.size());
		int i = 0;
		for (i = 0; i < trueVars.size(); i++) {
			logger.info("assigning true to " + this.trueVars.get(i));
			root.assignTruthValue(this.trueVars.get(i), true);
		}
		for (i = 0; i < falseVars.size(); i++) {
			logger.info("assigning false to " + this.falseVars.get(i));
			root.assignTruthValue(this.falseVars.get(i), false);
		}
		logger.info("exiting TransmissionQbf.assignTruthValues()");
	}

	public void reduceTree() {
		boolean continueReducing = true;
		
		logger.info("entering TransmissionQbf.reduceTree");
		while (continueReducing) {
		root.dump("");
			logger.info("reducing tree step begin");		
			continueReducing = root.jjtGetChild(0).reduce();	
			logger.info("reducing tree step end");		
		}
		logger.info("exiting TransmissionQbf.reduceTree");
	}

	public String traverseTree() {
		logger.info("entering TransmissionQbf.traverseTree");
		return root.jjtGetChild(0).traverse();
	}

	public static String allocateId() {
		idCounter++;
		return new Integer(idCounter).toString();
	}

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
