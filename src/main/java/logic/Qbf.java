package main.java.logic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import main.java.QPar;
import main.java.logic.parser.ParseException;
import main.java.logic.parser.Qbf_parser;
import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.TokenMgrError;

import org.apache.log4j.Logger;

/**
* A QBF object contains one QBF as well as methods to split it up into subQBFs
* and merging subresults back together
*
*/
public class Qbf {

    static Logger logger = Logger.getLogger(Qbf.class);

	Heuristic h = null;
	private static int id = 0;
	private int receivedResults = 0;
	private boolean satisfiable	= false;
	private boolean solved		= false;
	private DTNode decisionRoot = null;
	private ArrayList<TransmissionQbf> subQbfs	= new ArrayList<TransmissionQbf>();
	private ArrayList<Boolean> qbfResults		= new ArrayList<Boolean>();
	private ArrayList<Boolean> resultAvailable	= new ArrayList<Boolean>();
	private ArrayList<Boolean> resultProcessed	= new ArrayList<Boolean>();
	private Vector<Integer> decisionVars = new Vector<Integer>();
	private HashMap<Integer, Integer> literalCount = new HashMap<Integer, Integer>();	
	public Vector<Integer> eVars = new Vector<Integer>();
	public Vector<Integer> aVars = new Vector<Integer>();
	public Vector<Integer> vars  = new Vector<Integer>();
	public SimpleNode root = null;
	private String op; // TODO
	
	/**
	* constructor
	* @param filename The file containing the QBF that will be stored in this object
	* @throws IOException 
	*/
	public Qbf(String filename) throws IOException {
		logger.setLevel(QPar.logLevel);

		Qbf_parser parser;

		id++;

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
			logger.debug("Begin parsing...");
			parser.Input();	
			logger.debug("Succesful parse");
			literalCount = parser.getLiteralCount();
			this.eVars = parser.getEVars();
			this.aVars = parser.getAVars();
			this.vars = parser.getVars();
			root = parser.getRootNode();
			// root.dump("");
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
		logger.debug("Finished reading a QBF from " + filename);
	}

	/**
	* split a QBF to two or more subQBFs by assigning truth values to some of
	* the variables.
	* @param n Number of subformulas to return
	* @return A list of n TransmissionQbfs, each a subformula of the whole QBF
	*/
	public synchronized List<TransmissionQbf> splitQbf(int n, Heuristic h) {
		int i,j;
		TransmissionQbf tmp;
		Vector<Integer> tempVars = new Vector<Integer>();
		int numVarsToChoose = new Double(Math.log(n)/Math.log(2)).intValue();
		boolean[][] decisionArray = new boolean[n][numVarsToChoose];		
		
		// running the selected heuristic						
		tempVars = h.decide(this);

		// throw away the vars that are too much
		for(i = 0; i < numVarsToChoose; i++) {
			decisionVars.add(tempVars.get(i));
		}

		// generating a truth table
		logger.debug("generating truth table");
		for (j = 0; j < numVarsToChoose; j++) {
			decisionArray[0][j] = true;
			logger.debug(decisionArray[0][j]);
		}

		for(i = 1; i < n; i++) {
			for(j = 0; j < numVarsToChoose; j++) {
				if(i % (Math.pow(2,j)) == 0) {
					decisionArray[i][j] = !decisionArray[i-1][j];
				} 
				else {
					decisionArray[i][j] = decisionArray[i-1][j];
				}
				logger.debug(decisionArray[i][j]);
			}
			logger.debug("------");
		}
	
		// creating the root of the decision node
		if (aVars.contains(decisionVars.get(0))) {
			decisionRoot = new DTNode("&");
		} else if (eVars.contains(decisionVars.get(0))) {
			decisionRoot = new DTNode("|");
		}

		// for every var that is going to get truth-assigned, add a layer on
		// the tree, with AND or OR nodes, depending on the occurance of that
		// var in either eVars or aVars
		for (i=1; i < decisionVars.size(); i++) {
			if (aVars.contains(decisionVars.get(i))) {
				decisionRoot.addLayer("&");
			}else if (eVars.contains(decisionVars.get(i))) {
				decisionRoot.addLayer("|");
			}
		}
		
		// finally gather all leave nodes...
		Vector<DTNode> leafNodes = decisionRoot.getLeafNodes();
		// ...and add the subformula IDs as their children
		i = id * 1000;
		for (DTNode dtmp : leafNodes) {
			dtmp.addChild(new DTNode(i++));
			dtmp.addChild(new DTNode(i++));
		}

		// generating n TransmissionQBFs
		for (i = 0; i < n; i++) {
			tmp = new TransmissionQbf();
			
			qbfResults.add(i, false);
			resultAvailable.add(i, false);
			resultProcessed.add(i, false);
						
			tmp.setId((new Integer(id * 1000 + i)).toString());
			tmp.setRootNode(root);
			
			for (j = 0; j < numVarsToChoose; j++) {
				this.eVars.remove(decisionVars.get(j));
				this.aVars.remove(decisionVars.get(j));
				if (decisionArray[i][j]) {
					tmp.addToTrueVars(decisionVars.get(j));
				}
				else {
					tmp.addToFalseVars(decisionVars.get(j));
				}
			}

			tmp.setEVars(this.eVars);
			tmp.setAVars(this.aVars);
			tmp.setVars(this.vars);	

			tmp.checkQbf();
			subQbfs.add(tmp);
		}
		return subQbfs;
	}

	/**
	* merge 
	* the variables.
	* @param id Identifier of a certain subformula (= index in the subQbfs List)
	* @param result The result of the evaluated subformula
	* @return TRUE if the formula is already solved, FALSE if otherwise
	*/
	public synchronized boolean mergeQbf(String id, boolean result) {
		DTNode tmp = null;
	
		// find the corresponding node in the decisiontree
		tmp = decisionRoot.getNode(Integer.parseInt(id));
		
		// set the nodes truth value
		tmp.setTruthValue(result);

		// reduce the tree
		tmp.reduce();

		// check the root for a truth value and return
		return decisionRoot.hasTruthValue();
	}

	/**
	* getter method for solved
	* @return TRUE if there's a result, FALSE otherwise
	*/
	public synchronized boolean isSolved() {
		return decisionRoot.hasTruthValue();
	}
	
	/**
	* getter method for satisfiable
	* @return TRUE the QBF is satisfiable, FALSE if not
	*/
	public synchronized boolean getResult() {
		return decisionRoot.getTruthValue();
	}

	public HashMap<Integer, Integer> getLiteralCount() {
		return literalCount;
	}

	public void setHeuristic(Heuristic h) {
		this.h = h;
	}
}
