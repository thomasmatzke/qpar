package main.java.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
* A QBF object contains one QBF as well as methods to split it up into subQBFs
* and merging subresults back together
*
*/
public class Qbf {

	Heuristic h = null;
	File file;
//	Tree solvingTree = new Tree(); // obsolete?
	private String qbfString;
	private String filename;
	private static int id = 0;
	private int receivedResults = 0;
	private boolean satisfiable	= false;
	private boolean solved		= false;
	private List<TransmissionQbf> subQbfs		= new ArrayList<TransmissionQbf>();
	private ArrayList<Boolean> qbfResults		= new ArrayList<Boolean>();
	private ArrayList<Boolean> resultAvailable	= new ArrayList<Boolean>();
	private ArrayList<Boolean> resultProcessed	= new ArrayList<Boolean>();
	
	/**
	* constructor
	* @param filename The file containing the QBF that will be stored in this object
	*/
	public Qbf(String filename) {
		id++;
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

// syntax checking should now be done in the parser		
//		// maybe there will be a syntaxcheck somewhesre in the future
//		if (!checkQbfSyntax(qbfString)) {
//			System.out.println(filename + " seems to have syntax errors. Aborting.");
//			System.exit(1);
//		}

		System.out.println("Finished reading a QBF from " + filename);
		
	}

	/**
	* split a QBF to two or more subQBFs by assigning truth values to some of
	* the variables.
	* @param n Number of subformulas to return
	* @return A list of n TransmissionQbfs, each a subformula of the whole QBF
	*/
	public List<TransmissionQbf> splitQbf(int n) {

		TransmissionQbf tmp;
		
		for (int i = 0; i < n; i++) {
			qbfResults.add(i, false);
			resultAvailable.add(i, false);
			resultProcessed.add(i, false);
			
			tmp = new TransmissionQbf();
			tmp.setId((new Integer(id * 1000 + i)).toString());
			subQbfs.add(tmp);
			
		}
		// do stuff
		// TODO (returns empty TransmissionQbfs until this works)
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
	
		// for testing
		receivedResults++;
		if (receivedResults < subQbfs.size())
			return false;	
		solved = true;
		satisfiable = result;
		return true;
	}

	/**
	* getter method for solved
	* @return TRUE if there's a result, FALSE otherwise
	*/
	public boolean isSolved() {
		// if solved = FALSE, go through the list of unused results and, if
		// there are any, merge them. Then check again for solved and return it.
		if (solved == false) {
			for (int i = 0; i < subQbfs.size(); i++) {
				if (!resultProcessed.get(i)) {
					// mergeQbf(i, qbfResults.get(i));
				}
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

// should be in the parser now
//	/**
//	* Syntax check for a QBF string. Always true for now.
//	* @param filename Filename of the boole file
//	*/
//	public boolean checkQbfSyntax(String qbfString) {
//		return true;
//	}		

	public void setHeuristic(Heuristic h) {
		this.h = h;
	}
}
