package main.java.master.logic.heuristic;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class DependencyNode {

	static Logger logger = Logger.getLogger(DependencyNode.class);
	
	public DependencyNode(Integer variable, NodeType type) {
		this.variable = variable;
		this.type = type;
		if(type == NodeType.ROOT) {
			this.setDepth(0);
		}
	}
	
	public enum NodeType { ROOT, UNIVERSAL, EXISTENTIAL }
	
	private Integer depth;
	public NodeType type;
	public Integer variable;
	public ArrayList<DependencyNode> children = new ArrayList<DependencyNode>(); 
	

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
		for(DependencyNode d : children) {
			d.setDepth(depth+1);
		}
	}
	
	public String dump() {
		
		String indent = "";
		for(int i = 0; i < this.getDepth(); i++)
			indent += "  ";
		
		String s = indent + this + "\n";
		
		switch(children.size()) {
			case 0:
				return s;
			case 1:
				s += children.get(0).dump();
				return s;
			case 2:
				s += children.get(0).dump();
				s += children.get(1).dump();
				return s;
			default:
				assert(false);
				return null;
		}
	}
	
	public String toString() {
		return "(" + this.type + ", " + this.variable + ")";
	}
	
	public void addChild(DependencyNode child) {
		if(this.getDepth() != null)
			child.setDepth(this.getDepth()+1);
		children.add(child);
	}
	
	/**
	 * True if subtree (this node included) has all the same quantifiers
	 * @return
	 */
	public boolean homogenousSubtree() {
		switch(children.size()) {
			case 0:
				return true;
			case 1:
				return (this.type == children.get(0).type && children.get(0).homogenousSubtree());
			case 2:
				return 	((this.type == children.get(0).type) && children.get(0).homogenousSubtree() &&
						 (this.type == children.get(1).type) && children.get(1).homogenousSubtree());
			default:
				logger.fatal("A DependencyNode must not have more than 3 childnodes.");
				System.exit(-1);
				return false;
		}
				
	}
	
	/**
	 * Condenses subtree (packing together equal quantifiers in succession, etc)
	 * @return Rootnode of a condesed subtree
	 */
	public CondensedDependencyNode condense() {
		
		// IF the whole subtree is made up of same quantifiers return one node with them all
		// included
		if(this.homogenousSubtree()) {
			CondensedDependencyNode n = new CondensedDependencyNode(this.type);
			for(DependencyNode d : this.allSubnodes()) {
				n.variables.add(d.variable);
			}
			return n;
		}
		
		
		
		switch(children.size()) {
			case 0:
				// Cant be. homogenousSubtree would have returned already
				assert(false);
				logger.fatal("Algorithm fail... Contact Author.");
				System.exit(-1);
				return null;
			case 1:
				CondensedDependencyNode n = new CondensedDependencyNode(this.type);
				DependencyNode current = this;
				
				while(current.type == this.type && current.children.size() == 1) {
					n.variables.add(current.variable);
					
					current = current.children.get(0);
				}
				CondensedDependencyNode cd = current.condense();
				n.addChild(cd);
				
				if(n.type == NodeType.ROOT)
					n.setDepth(0);
				return n;
			case 2:
				CondensedDependencyNode n1 = new CondensedDependencyNode(this.type);
				n1.variables.add(this.variable);
				n1.addChild(this.children.get(0).condense());
				n1.addChild(this.children.get(1).condense());
				return n1;
			default:
				assert(false);
				logger.fatal("A DependencyNode must not have more than 3 childnodes.");
				System.exit(-1);
				return null;
		}
		
			
	}
	
	/**
	 * Returns all subnodes, including this
	 * @return Subnodes of this
	 */
	public ArrayList<DependencyNode> allSubnodes() {
		switch(children.size()) {
			case 0:
				ArrayList<DependencyNode> n0 = new ArrayList<DependencyNode>();
				n0.add(this);
				return n0;
			case 1:
				ArrayList<DependencyNode> n1 = children.get(0).allSubnodes();
				n1.add(this);
				return n1;
			case 2:
				ArrayList<DependencyNode> n20 = children.get(0).allSubnodes();
				ArrayList<DependencyNode> n21 = children.get(1).allSubnodes();
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
