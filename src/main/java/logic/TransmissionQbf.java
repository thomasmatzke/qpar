package main.java.logic;

import java.util.Vector;
import main.java.logic.parser.SimpleNode;
import java.io.Serializable;

public class TransmissionQbf implements Serializable {
	private SimpleNode root = null;
	private static int idCounter = 0;
	private String id;
	private static Vector<Integer> eVars = new Vector<Integer>();
	private static Vector<Integer> aVars = new Vector<Integer>();
	private static Vector<Integer> vars = new Vector<Integer>();
	private static Vector<Integer> trueVars = new Vector<Integer>();
	private static Vector<Integer> falseVars = new Vector<Integer>();

	public void setTrueVars( Vector<Integer> v) {
		this.trueVars = v;
	}

	public void setFalseVars( Vector<Integer> v) {
		this.falseVars = v;
	}

	public Vector<Integer> getEVars() {
		return eVars;
	}

	public Vector<Integer> getAVars() {
		return aVars;
	}

	public Vector<Integer> getVars() {
		return vars;
	}

	public void setEVars(Vector<Integer> v) {
		this.eVars = v;
	}

	public void setAVars(Vector<Integer> v) {
		this.aVars = v;
	}

	public void setVars(Vector<Integer> v) {
		this.vars = v;
	}

	public void assignTruthValues() {
		int i = 0;
		for (i = 0; i < trueVars.size(); i++)
			root.assignTruthValue(trueVars.get(i), true);
		for (i = 0; i < falseVars.size(); i++)
			root.assignTruthValue(trueVars.get(i), false);
	}

	public void reduceTree() {
		boolean reducable = true;
		
		while (reducable) {
			reducable = root.reduceTree();	
		}
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
