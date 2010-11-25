package main.java.logic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import main.java.QPar;
import main.java.logic.heuristic.DependencyNode;
import main.java.logic.heuristic.Heuristic;
import main.java.logic.parser.ParseException;
import main.java.logic.parser.Qbf_parser;
import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.TokenMgrError;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
* A QBF object contains one QBF as well as methods to split it up into subQBFs
* and merging subresults back together
*
*/
public class Qbf {
	
    static Logger logger = Logger.getLogger(Qbf.class);

	Heuristic h = null;
	private static int idCounter = 0;
	public DTNode decisionRoot = null;
	private HashMap<Integer, Integer> literalCount = new HashMap<Integer, Integer>();	
	public Vector<Integer> eVars = new Vector<Integer>();
	public Vector<Integer> aVars = new Vector<Integer>();
	public Vector<Integer> vars  = new Vector<Integer>();
	public SimpleNode root = null;
	public int id;
	
	public DependencyNode dependencyGraphRoot;
		
	private Object mergeLock = new Object();
	
	/**
	* constructor
	* @param filename The file containing the QBF that will be stored in this object
	* @throws IOException 
	*/
	public Qbf(String filename) throws IOException {
		assert(filename != null);
		logger.setLevel(QPar.logLevel);
		idCounter++;
		this.id = idCounter;
		
		Qbf_parser parser;
		long start = System.currentTimeMillis();
		
		parser = new Qbf_parser(new FileInputStream(filename));
		
		
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
			//root.dump("");
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
		long end = System.currentTimeMillis();
		logger.debug("Existential quantified Variables: " + eVars);
		logger.debug("Number of e.q.vs: " + eVars.size());
		logger.debug("Universally quantified Variables: " + aVars);
		logger.debug("Number of u.q.vs: " + aVars.size());
		logger.debug("All variables: " + vars);
		logger.debug("Number of all v.: " + vars.size());
		logger.info("Finished parsing QBF from " + filename + ", Took: " + (end-start)/1000 + " seconds.");
		
		logger.info("Generating dependency graph...");
		start = System.currentTimeMillis();
		dependencyGraphRoot = this.root.dependencyTree()[0];
		end = System.currentTimeMillis();
		logger.info("Dependency graph generated. Took " + (end-start)/1000 + " seconds.");
		if(QPar.logLevel == Level.DEBUG)
			logger.debug("Dependencyree: \n" + dependencyGraphRoot.dump());
	}

	/**
	* split a QBF to two or more subQBFs by assigning truth values to some of
	* the variables.
	* V2: Uses exactly n cores now
	* @param n Number of subformulas to return
	* @return A list of n TransmissionQbfs, each a subformula of the whole QBF
	*/
	public synchronized List<TransmissionQbf> splitQbf(int n, Heuristic h) {
		logger.info("Splitting into " + n + " subformulas...");
		long start = System.currentTimeMillis();
		Integer[] order = h.getVariableOrder().toArray(new Integer[0]);
		logger.info("Heuristic returned variable-assignment order: " + Arrays.toString(order));
			
		int leafCtr = 1;
		ArrayDeque<DTNode> leaves = new ArrayDeque<DTNode>();
		decisionRoot = new DTNode(null);
		leaves.addFirst(decisionRoot);
		
		// Generate the tree
		logger.info("Generating decision tree...");
		do {
			DTNode leaf 		= leaves.pollLast();
			Integer splitVar 	= order[leaf.getDepth()]; 
			if(aVars.contains(splitVar)) {
				leaf.setType(DTNode.DTNodeType.AND);
			} else if(eVars.contains(splitVar)) {
				leaf.setType(DTNode.DTNodeType.OR);
			}
			DTNode negChild = new DTNode(DTNode.DTNodeType.TQBF);
			negChild.variablesAssignedFalse.add(splitVar);
			negChild.setParent(leaf);
			negChild.getDepth();
			DTNode posChild = new DTNode(DTNode.DTNodeType.TQBF);
			posChild.variablesAssignedTrue.add(splitVar);
			posChild.setParent(leaf);
			posChild.getDepth();
			leaf.addChild(negChild); leaf.addChild(posChild);
			leaves.addFirst(negChild); leaves.addFirst(posChild);
			leafCtr++;
		} while(leafCtr < n);
		
		logger.info("\n" + decisionRoot.dump());
		
		assert(leaves.size() == n);
		
		logger.info("Generating TransmissionQbfs...");
		List<TransmissionQbf> tqbfs = new ArrayList<TransmissionQbf>();
		// Generate ids for leaves and corresponding tqbfs
		int idCtr = 0;
		for(DTNode node : leaves) {
			String id = Integer.toString(this.id) + "." +  Integer.toString(idCtr++);
			node.setId(id);
			TransmissionQbf tqbf = new TransmissionQbf();
			tqbf.setId(id);
			tqbf.falseVars.addAll(node.variablesAssignedFalse);
			tqbf.trueVars.addAll(node.variablesAssignedTrue);
			tqbf.setRootNode(root);
			tqbf.setEVars(this.eVars);
			tqbf.setAVars(this.aVars);
			Vector<Integer> tmpVars = new Vector<Integer>();
			tmpVars.addAll(aVars);
			tmpVars.addAll(eVars);
			tqbf.setVars(tmpVars);
			tqbfs.add(tqbf);
		}
		assert(tqbfs.size() == n);
		long end = System.currentTimeMillis();
		logger.info("Formula splitted. Took " + (end-start)/1000 + " seconds.");
		return tqbfs;
	}


	/**
	* merge 
	* the variables.
	* @param id Identifier of a certain subformula (= index in the subQbfs List)
	* @param result The result of the evaluated subformula
	* @return TRUE if the formula is already solved, FALSE if otherwise
	*/
	public boolean mergeQbf(String tqbfId, boolean result) {
		synchronized(mergeLock) {
			if(decisionRoot.hasTruthValue())
				return true;
			
			// find the corresponding node in the decisiontree
			DTNode tmp = decisionRoot.getNode(tqbfId);
			
			
			if(tmp == null) {
				// The node has been cut off from the root by another reduce()
				// The result is thus irrelevant
				return decisionRoot.hasTruthValue();
			}
						
			// set the nodes truth value
			tmp.setTruthValue(result);
	
			// reduce the tree
			tmp.reduce();
	
			// check the root for a truth value and return
			return decisionRoot.hasTruthValue();
		}
	}
	
	
	public boolean isUniversalQuantified(Integer v) {
		return aVars.contains(v);
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
