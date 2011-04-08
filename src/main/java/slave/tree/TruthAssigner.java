package main.java.slave.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import main.java.master.logic.parser.Node;
import main.java.master.logic.parser.SimpleNode;
import main.java.master.logic.parser.SimpleNode.NodeType;

import org.apache.log4j.Logger;

/**
 * Takes a SimpleNode and assigns truth values to variables in the subtree
 * @author thomasm
 *
 */
public class TruthAssigner {
	
	static Logger logger = Logger.getLogger(TruthAssigner.class);
	
	SimpleNode root;
	ArrayList<Integer> trueVars;
	ArrayList<Integer> falseVars;
	
	public TruthAssigner(SimpleNode root, ArrayList<Integer> trueVars, ArrayList<Integer> falseVars) {
		this.root = root;
//logger.info("new TruthAssigner. rootNode: " + this.root.hashCode());
		this.trueVars = trueVars;
		this.falseVars = falseVars;
	}
	
	/**
	 * Assigns nodes and returns them
	 * @return
	 */
	public ArrayDeque<SimpleNode> assign() {
		TreeStatistic preStat = new TreeStatistic(this.root);
		logger.info("Assigning " + Arrays.toString(trueVars.toArray()) + ", " + Arrays.toString(falseVars.toArray()));
		ArrayDeque<SimpleNode> assigned = assignNode(root);
		logger.info("Assigned " + assigned.size() + " nodes.");
		TreeStatistic postStat = new TreeStatistic(this.root);
		
		int deltaAnd = postStat.getNumAnd() - preStat.getNumAnd();
		int deltaOr = postStat.getNumOr() - preStat.getNumOr();
		int deltaTrue = postStat.getNumTrue() - preStat.getNumTrue();
		int deltaFalse = postStat.getNumFalse() - preStat.getNumFalse();
		int deltaForall = postStat.getNumForall() - preStat.getNumForall();
		int deltaExists = postStat.getNumExists() - preStat.getNumExists();
		int deltaVar = postStat.getNumVar() - preStat.getNumVar();
		int deltaNot = postStat.getNumNot() - preStat.getNumNot();
				
		logger.info("Assign Statistic: DeltaAnd: " + deltaAnd + 
				", DeltaOr: " + deltaOr +
				", DeltaTrue: " + deltaTrue + 
				", DeltaFalse: " + deltaFalse + 
				", DeltaNot: " + deltaNot + 
				", DeltaForall: " + deltaForall + 
				", DeltaExists: " + deltaExists + 
				", DeltaVar: " + deltaVar);
		
		return assigned;
	}
		
	private ArrayDeque<SimpleNode> assignNode(SimpleNode node) {
		ArrayDeque<SimpleNode> ret = new ArrayDeque<SimpleNode>();
		if (node.nodeType.equals(NodeType.VAR)) {
			if(trueVars.contains(node.getVar())) {
				node.setNodeType(NodeType.TRUE);
				ret.add(node);
			} else if(falseVars.contains(node.getVar())) {
				node.setNodeType(NodeType.FALSE);
				ret.add(node);
			}
			
		} else {
			for(Node n : node.children) {
				ret.addAll(assignNode((SimpleNode) n));
			}
		}
		return ret;
	}
	
}
