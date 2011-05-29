package qpar.slave.tree;


import org.apache.log4j.Logger;

import qpar.master.logic.parser.Node;
import qpar.master.logic.parser.SimpleNode;

public class TreeStatistic {
	static Logger logger = Logger.getLogger(TreeStatistic.class);

	SimpleNode root = null;
	
	private int numVar, numAnd, numOr, numForall, numExists, numFalse, numTrue, numNot;
	
	public TreeStatistic(SimpleNode node) {
		this.root = node;
		count(this.root);
	}
			
	public int getNumNot() {
		return numNot;
	}

	public int getNumVar() {
		return numVar;
	}

	public int getNumAnd() {
		return numAnd;
	}

	public int getNumOr() {
		return numOr;
	}

	public int getNumForall() {
		return numForall;
	}

	public int getNumExists() {
		return numExists;
	}

	public int getNumFalse() {
		return numFalse;
	}

	public int getNumTrue() {
		return numTrue;
	}

	private void count(SimpleNode node) {
		switch(node.getNodeType()) {
			case FORALL:
				numForall++; break;
			case EXISTS:
				numExists++; break;
			case TRUE:
				numTrue++; break;
			case FALSE:
				numFalse++; break;
			case AND:
				numAnd++; break;
			case OR:
				numOr++; break;
			case VAR:
				numVar++; break;
			case NOT:
				numNot++; break;
			case START:
				break;
			default:
				logger.error("Encountered illegal NodeType: " + node.getNodeType());
				throw new IllegalStateException();
		}
		
		if(node.jjtGetNumChildren() != 0) {
			for(Node n : node.children) {
				count(((SimpleNode)n));
 			}
		} 		
	}
	
}
