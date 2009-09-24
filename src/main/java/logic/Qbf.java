package main.java.logic;

import java.util.*;
import java.io.*;

/**
* A QBF object contains one QBF as well as methods to split it up into subQBFs
* and merging subresults back together
*
*/
public class Qbf {

	Heuristic h = null;
	File file;
//	Tree solvingTree = new Tree();
	private String filename;
	private String qbfString;
	private boolean solved		= false;
	private boolean satisfiable	= false;
	private ArrayList<String> subQbfs			= new ArrayList<String>();
	private ArrayList<Boolean> qbfResults		= new ArrayList<Boolean>();
	private ArrayList<Boolean> resultAvailable	= new ArrayList<Boolean>();
	private ArrayList<Boolean> resultProcessed	= new ArrayList<Boolean>();

	/**
	* constructor
	* @param filename The file containing the QBF that will be stored in this object
	*/
	Qbf(String filename) {
		this.filename = filename;
		file = new File(filename);
		
		// check if file exists
		if (!file.exists()) {
			System.out.println("There is no file named " + filename);
			System.exit(1);
		}

		// read from file and store formula in qbfString
		try {
			BufferedReader qbfBuffer =  new BufferedReader(new FileReader(file));
			qbfString = qbfBuffer.readLine();
		} catch (Exception e) {
			System.out.println("Something went wrong reading from " + filename);
			System.exit(1);
		}
		
		// maybe there will be a syntaxcheck somewhere in the future
		if (!checkQbfSyntax(qbfString)) {
			System.out.println(filename + " seems to have syntax errors. Aborting.");
			System.exit(1);
		}

		System.out.println("Finished reading a QBF from " + filename + " (" + qbfString + ")");
	}

	/**
	* split a QBF to two or more subQBFs by assigning truth values to some of
	* the variables.
	* @param n Number of subformulas to return
	* @return A list of n strings, each a subformula of the whole QBF
	*/
	List<String> splitQbf(int n) {
		for (int i = 0; i < n; i++) {
			qbfResults.add(i, false);
			resultAvailable.add(i, false);
			resultProcessed.add(i, false);
			
			subQbfs.add(i, qbfString);
			
		}
		// do stuff
		return subQbfs;
	}

	/**
	* merge 
	* the variables.
	* @param id Identifier of a certain subformula (= index in the subQbfs List)
	* @param result The result of the evaluated subformula
	* @return TRUE if the formula is already solved, FALSE if otherwise
	*/
	public boolean mergeQbf(int id, boolean result) {

		resultAvailable.set(id, true);
//		Node op1 = solvingTree.search(id);
//		Node operand = solvingTree.getParentNode(op1);
//		Node op2 = solvingTree.getSibling(op1);

//		// if a subresult can't be used at the time it arrives at the master,
//		// it will be held back for later use		
//		if ((op2.getKey() != "TRUE") || (op2.getKey() != "FALSE")) {
//			return;
//		} else {
//			resultProcessed.set(op1.getID(), true);
//			resultProcessed.set(op2.getID(), true);

//			if (operand.getKey().equals("AND")) {
//				if (result && op2.toBool()) {
//					operand.setKey("TRUE");
//				} else {
//					operand.setKey("FALSE");					
//				}
//			} else if (operand.getKey().equals("OR")) {
//				if (result || op2.toBool()) {
//					operand.setKey("TRUE");
//				} else {
//					operand.setKey("FALSE");					
//				}
//			}
////			solvingTree.remove(op1);
////			solvingTree.remove(op2);
//		}
		
	
		// if a result is merged, set resultProcessed(id) and resultAvailable(id)
		// to TRUE and qbfResult(id) to result
	
		// if the formula is solved, set solved to TRUE
		return false;
	}

	/**
	* getter method for solved
	* @return TRUE if there's a result, FALSE otherwise
	*/
	public boolean isSolved() {
		// go through the list of unused results and, if there are any, merge
		// them.
		
		for (int i = 0; i < subQbfs.size(); i++) {
			if (!resultProcessed.get(i)) {
				// mergeQbf(i, qbfResults.get(i));
			}
		}

		return solved;
	}
	
	/**
	* getter method for satisfiable
	* @return TRUE the QBF is satisfiable, FALSE if not
	*/
	public boolean getResult() {
		return satisfiable;
	}

	/**
	* Syntax check for a QBF string. Always true for now.
	* @param filename Filename of the boole file
	*/
	public boolean checkQbfSyntax(String qbfString) {
		return true;
	}		

	public void setHeuristic(Heuristic h) {
		this.h = h;
	}

}
