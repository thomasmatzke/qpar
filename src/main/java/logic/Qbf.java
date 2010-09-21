package main.java.logic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import main.java.QPar;
import main.java.logic.heuristic.DependencyNode;
import main.java.logic.heuristic.Heuristic;
import main.java.logic.parser.ParseException;
import main.java.logic.parser.Qbf_parser;
import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.TokenMgrError;
import main.java.master.MasterDaemon;

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
	
	private Stack<SimpleNode> quantifierStack;
	
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
		logger.info("Existential quantified Variables: " + eVars);
		logger.info("Number of e.q.vs: " + eVars.size());
		logger.info("Universally quantified Variables: " + aVars);
		logger.info("Number of u.q.vs: " + aVars.size());
		logger.info("All variables: " + vars);
		logger.info("Number of all v.: " + vars.size());
		logger.debug("Finished reading a QBF from " + filename);
		
		this.generateDependencyGraph();
	}

	/**
	* split a QBF to two or more subQBFs by assigning truth values to some of
	* the variables.
	* V2: Uses exactly n cores now
	* @param n Number of subformulas to return
	* @return A list of n TransmissionQbfs, each a subformula of the whole QBF
	*/
	public synchronized List<TransmissionQbf> splitQbf(int n, Heuristic h) {
		Integer[] order = h.getVariableOrder().toArray(new Integer[0]);
		logger.info("Heuristic returned variable-assignment order: " + Arrays.toString(order));
		
		logger.info("Splitting into " + n + " subformulas...");
		
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
		
		//logger.info("\n" + decisionRoot.dump());
		
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

	/**
	 * Generates a dependency graph which is used by the heuristics 
	 * @throws SQLException 
	 */
	public void generateDependencyGraph() {
		logger.info("Generating dependency graph...");
		long start = System.currentTimeMillis();
		DependencyNode.registry = new HashMap<Integer, DependencyNode>();
		this.quantifierStack = new Stack<SimpleNode>();
		this.traverse(this.root);
		long end = System.currentTimeMillis();
		assert(DependencyNode.registry.size() == vars.size()+1);
		logger.info("Dependency graph generated. Took " + (end-start)/1000 + " seconds.");
	}
	
	private void traverse(SimpleNode node) {
		logger.debug("Encountered " + node.getNodeType() + " node.");
		switch(node.getNodeType()) {
			case START:
				this.dependencyGraphRoot = new DependencyNode(0, DependencyNode.NodeType.ROOT);
				this.quantifierStack.push(node);
				DependencyNode.registry.put(0, this.dependencyGraphRoot);
				traverse((SimpleNode)node.jjtGetChild(0));
				break;
			case FORALL:
			case EXISTS:
				logger.debug("Calculating dependencies of node " + node);
				Set<Integer> deps = getDependencies(quantifierStack, node.getNodeType());
				for(Integer dep : deps) {
					DependencyNode.NodeType t = null;
					if(node.getNodeType() == SimpleNode.NodeType.EXISTS){
						t = DependencyNode.NodeType.EXISTENTIAL;
					} else if(node.getNodeType() == SimpleNode.NodeType.FORALL) {
						t = DependencyNode.NodeType.UNIVERSAL;
					} else {
						assert(false);
					}
						
					DependencyNode.registry.get(dep).addChild(new DependencyNode(node.var, t));
				}
				
				this.quantifierStack.push(node);								
				traverse((SimpleNode)node.jjtGetChild(0));								
				this.quantifierStack.pop();
				
				break;
			case NOT:
				traverse((SimpleNode)node.jjtGetChild(0));
				break;
			case AND:
			case OR:
				traverse((SimpleNode)node.jjtGetChild(0));
				traverse((SimpleNode)node.jjtGetChild(1));
				break;
			case VAR:
				break;
			default:
				logger.error("Encountered non-expected NodeType while traversing the tree for dependency-graph generation.");
				MasterDaemon.bailOut();	
		}
		
				
	}
	
	private Set<Integer> getDependencies(Stack<SimpleNode> stack, SimpleNode.NodeType currentQuantifier) {
		Set<Integer> ret = new HashSet<Integer>();
		Stack<SimpleNode> stackClone = (Stack<SimpleNode>) stack.clone();
		logger.debug("Dependency stack: " + stackClone);
		
		// Search for first occurence of other quantifier
		while(stackClone.peek().nodeType == currentQuantifier) { stackClone.pop(); }
		
		// Return all quants in the next block of the NOT-currentQuantifier
		while(!stackClone.empty() && stackClone.peek().nodeType != currentQuantifier) {
			SimpleNode n = stackClone.pop();
			if(n.getNodeType() == SimpleNode.NodeType.START) {
				ret.add(0);
			} else {
				ret.add(n.getNodeVariable()); 
			}		
		}
							
		return ret;
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
