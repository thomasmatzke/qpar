package main.java.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

import main.java.master.MasterDaemon;
import main.java.logic.parser.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
/**
* A QBF object contains one QBF as well as methods to split it up into subQBFs
* and merging subresults back together
*
*/
public class Qbf {
    static Logger logger = Logger.getLogger(Qbf.class);
    {
		logger.setLevel(Level.DEBUG);
	}

	Heuristic h = null;
	File file;
	//	Tree solvingTree = new Tree(); // obsolete?
	private String qbfString;
	private String filename;
	private static int id = 0;
	private int receivedResults = 0;
	private boolean satisfiable	= false;
	private boolean solved		= false;
	private ArrayList<TransmissionQbf> subQbfs	= new ArrayList<TransmissionQbf>();
	private ArrayList<Boolean> qbfResults		= new ArrayList<Boolean>();
	private ArrayList<Boolean> resultAvailable	= new ArrayList<Boolean>();
	private ArrayList<Boolean> resultProcessed	= new ArrayList<Boolean>();
	private HashMap<Integer, Integer> literalCount  = new HashMap<Integer, Integer>();	
	private Vector<Integer> eVars = new Vector<Integer>();
	private Vector<Integer> aVars = new Vector<Integer>();
	public Vector<Integer> vars  = new Vector<Integer>();
	private SimpleNode root = null;

	/**
	* constructor
	* @param filename The file containing the QBF that will be stored in this object
	* @throws IOException 
	*/
	public Qbf(String filename) throws IOException {
		Qbf_parser parser;

		id++;
		this.filename = filename;
		file = new File(filename);

		BufferedReader qbfBuffer =  new BufferedReader(new FileReader(file));

		try {
			parser = new Qbf_parser(new FileInputStream(filename));
		}
		catch (FileNotFoundException e) {
			logger.error("File not found: " + filename);
			return;
		}
		parser.ReInit(new FileInputStream(filename), null);

		// parse the formula, get various vectors of vars
		try {
			parser.Input();	
			logger.info("Succesful parse");
			literalCount = parser.getLiteralCount();
			this.eVars = parser.getEVars();
			this.aVars = parser.getAVars();
			this.vars = parser.getVars();
			root = parser.getRootNode();
		
		}
		catch (ParseException e) {
			logger.error("Parse error");			
			logger.error(e);
			return;
		}
		catch (TokenMgrError e) {
			logger.error(e);
			return;
		}
		logger.info("Finished reading a QBF from " + filename);
	}

	/**
	* split a QBF to two or more subQBFs by assigning truth values to some of
	* the variables.
	* @param n Number of subformulas to return
	* @return A list of n TransmissionQbfs, each a subformula of the whole QBF
	*/
	public List<TransmissionQbf> splitQbf(int n, Heuristic h) {
		int i,j;
		TransmissionQbf tmp;
		Vector<Integer> tempVars = new Vector<Integer>();
		Vector<Integer> decisionVars = new Vector<Integer>();
		Vector<Integer> trueVars = new Vector<Integer>();
		Vector<Integer> falseVars = new Vector<Integer>();
		int numVarsToChoose = new Double(Math.log(n)/Math.log(2)).intValue();
		boolean[][] decisionArray = new boolean[n][numVarsToChoose];		

		// running the selected heuristic						
		tempVars = h.decide(this);

		// throw away the vars that are too much
		for(i = 0; i < numVarsToChoose; i++) {
			decisionVars.add(tempVars.get(i));
		}

		// generating a truth table
		for (j = 0; j < numVarsToChoose; j++) decisionArray[0][j] = true;

		for(i = 1; i < n; i++) {
			for(j = 0; j < numVarsToChoose; j++) {
				if(i % (Math.pow(2,j)) == 0) {
					decisionArray[i][j] = !decisionArray[i-1][j];
				} 
				else {
					decisionArray[i][j] = decisionArray[i-1][j];
				}
			}
		}

		// generating n TransmissionQBFs
		for (i = 0; i < n; i++) {
			tmp = new TransmissionQbf();
			
			qbfResults.add(i, false);
			resultAvailable.add(i, false);
			resultProcessed.add(i, false);
						
			tmp.setId((new Integer(id * 1000 + i)).toString());
			tmp.setRootNode(root);

			tmp.setEVars(this.eVars);
			tmp.setAVars(this.aVars);
			tmp.setVars(this.vars);	
			
			for (j = 0; j < numVarsToChoose; j++) {
				if (decisionArray[i][j]) {
					tmp.addToTrueVars(decisionVars.get(j));
				}
				else {
					tmp.addToFalseVars(decisionVars.get(j));
				}
			}

			tmp.checkQbf();
			subQbfs.add(tmp);
		}

logger.info("checking subqbf list");
for (TransmissionQbf foo : subQbfs) foo.checkQbf();
logger.info("end");

		return subQbfs;
	}

	/**
	* merge 
	* the variables.
	* @param id Identifier of a certain subformula (= index in the subQbfs List)
	* @param result The result of the evaluated subformula
	* @return TRUE if the formula is already solved, FALSE if otherwise
	*/
	public boolean mergeQbf(String id, boolean result) {
//		resultAvailable.set(id, true);
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

	public HashMap<Integer, Integer> getLiteralCount() {
		return literalCount;
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
