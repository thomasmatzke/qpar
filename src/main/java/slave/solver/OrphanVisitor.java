package main.java.slave.solver;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import main.java.logic.Visitor;
import main.java.logic.parser.Node;
import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.SimpleNode.NodeType;

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
			quantifiedVars.remove(new Integer(node.getVar()));
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
