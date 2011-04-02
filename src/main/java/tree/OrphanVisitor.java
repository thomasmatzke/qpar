package main.java.tree;

import java.util.ArrayList;

import main.java.logic.parser.Node;
import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.SimpleNode.NodeType;

import org.apache.log4j.Logger;

public class OrphanVisitor implements Visitor {

	static Logger logger = Logger.getLogger(OrphanVisitor.class);
	
	ArrayList<Integer> quantifiedVars;
	
	public OrphanVisitor(ArrayList<Integer> quantifiedVars) {
		this.quantifiedVars = quantifiedVars;
	}
	
	@Override
	public void visit(SimpleNode node) {
		// Payload
		// Remove all variables from quantifiedVars we encounter.
		// The rest are orpahns
		if(node.getNodeType() == NodeType.VAR) {
			quantifiedVars.remove(Integer.valueOf(node.getVar()));
		}

		// Make it recursive
		if(node.children != null) {	
			for(Node n : node.children) {
				((SimpleNode)n).accept(this);
			}
		}
	}
	
	public ArrayList<Integer> getOrpahns() {
		return quantifiedVars;
	}

}
