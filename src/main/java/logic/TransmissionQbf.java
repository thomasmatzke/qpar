package main.java.logic;

import java.util.Vector;
import main.java.logic.parser.SimpleNode;
import java.io.Serializable;

public class TransmissionQbf implements Serializable {

	SimpleNode root = null;
	private static int idCounter = 0;
	private String id;
	private static Vector<Integer> eVars = new Vector<Integer>();
	private static Vector<Integer> aVars = new Vector<Integer>();
	private static Vector<Integer> vars = new Vector<Integer>();
	
	public static String allocateId() {
		idCounter++;
		return new Integer(idCounter).toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRootNode(SimpleNode n) {
		this.root = n;
	}

	/**
	* convert a qbf tree to a qpro string by first assigning the truth values to
	* the vars, followed by reducing and traversing
	* return String a formula in qpro format
	*/
	public String toQproString() {
		boolean reducable = true;
		String traversedTree = "";
		int i = 0;
		
		// assign truth values		
		//

		// reducing tree
		while (reducable) {
			reducable = root.reduceTree();	
		};

		// Convert internal tree to .qpro format				
		traversedTree += "\nQBF\n" + vars.size() + "\nq\n" + "a ";
		for (i=0; i < eVars.size(); i++) traversedTree += eVars.get(i) + " ";
		traversedTree += "\n" + "e ";
		for (i=0; i < aVars.size(); i++) traversedTree += aVars.get(i) + " ";
		traversedTree += root.traverse();
		traversedTree += "QBF";

		return traversedTree;
	}	
}
