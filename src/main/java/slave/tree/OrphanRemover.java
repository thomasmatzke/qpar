package main.java.slave.tree;

import java.util.HashSet;

import main.java.master.logic.parser.Node;
import main.java.master.logic.parser.SimpleNode;

import org.apache.log4j.Logger;

public class OrphanRemover {

	static Logger logger = Logger.getLogger(OrphanRemover.class);
	
	SimpleNode root = null;
	HashSet<Integer> variables;
	
	
	public OrphanRemover(SimpleNode node) {
		this.root = node;
		this.variables = this.root.getVariableSet();	
	}
	
	public void removeOrphans() { 
		this.removeOrphans(this.root);
	}
	
	private void removeOrphans(SimpleNode n) {
		if(n.isQuantifierNode() && !variables.contains(n.getVar()))
				n.cutOutQuantifierNode();
		
		if(n.jjtGetNumChildren() == 0)
			return;
		
		for(Node child : n.children) {
			removeOrphans(((SimpleNode)child));
		}
	}
			
}
