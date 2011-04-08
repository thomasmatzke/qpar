package main.java.master.logic.heuristic;

import java.util.ArrayList;

import main.java.master.logic.heuristic.DependencyNode.NodeType;

import org.apache.log4j.Logger;

/**
 * Used by getDecisionGroups
 * @author thomasm
 *
 */
public class CondensedDependencyNode {		
	// TODO: make this DRY -> GenericNode with common stuff from Condensed and DependencyNode
	static Logger logger = Logger.getLogger(CondensedDependencyNode.class);
	
	private Integer depth;
	public NodeType type;
	public ArrayList<Integer> variables = new ArrayList<Integer>();
	private ArrayList<CondensedDependencyNode> children = new ArrayList<CondensedDependencyNode>();
	
	public CondensedDependencyNode(DependencyNode.NodeType t) {
		this.type = t;
		if(type == NodeType.ROOT) {
			setDepth(0);
		}
	}
	
	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
		for(CondensedDependencyNode d : children) {
			d.setDepth(depth+1);
		}
	}
	
	public ArrayList<CondensedDependencyNode> getChildren() {
		return children;
	}
	
	public void addChild(CondensedDependencyNode n) {
		if(this.depth != null)
			n.depth = this.depth+1;
		children.add(n);
	}
	
	public String dump() {
		
		String indent = "";
		for(int i = 0; i < this.depth; i++)
			indent += "  ";
		
		String s = indent + this + "\n";
		switch(children.size()) {
			case 0:
				return s;
			case 1:
				s += children.get(0).dump();
				break;
			case 2:
				s += children.get(0).dump();
				s += children.get(1).dump();
				break;
			default:
				assert(false);
		
		}
		return s;

	}
	
	public String toString() {
		return type.toString() + "(" + variables.toString() + ", depth: " + this.depth + ")";
	}
	
	/**
	 * Returns all subnodes, including this
	 * @return Subnodes of this
	 */
	public ArrayList<CondensedDependencyNode> allSubnodes() {
		switch(children.size()) {
			case 0:
				ArrayList<CondensedDependencyNode> n0 = new ArrayList<CondensedDependencyNode>();
				n0.add(this);
				return n0;
			case 1:
				ArrayList<CondensedDependencyNode> n1 = children.get(0).allSubnodes();
				n1.add(this);
				return n1;
			case 2:
				ArrayList<CondensedDependencyNode> n20 = children.get(0).allSubnodes();
				ArrayList<CondensedDependencyNode> n21 = children.get(1).allSubnodes();
				n20.addAll(n21);
				n20.add(this);
				return n20;			
			default:
				assert(false);
				logger.fatal("A DependencyNode must not have more than 3 childnodes.");
				System.exit(-1);
				return null;
		}
	}
	
}