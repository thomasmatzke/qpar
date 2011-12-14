/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package qpar.master;

import java.util.ArrayList;

import org.apache.log4j.Logger;

// a simple node class
public class DTNode {

    static Logger logger = Logger.getLogger(DTNode.class);
	private String id = null;
	private DTNode leftChild = null;
	private DTNode rightChild = null;
	private DTNode parent = null;
	private int depth = -1;
	public DTNodeType type;
	public ArrayList<Integer> variablesAssignedTrue 	= new ArrayList<Integer>();
	public ArrayList<Integer> variablesAssignedFalse 	= new ArrayList<Integer>();
	
	public enum DTNodeType { AND, OR, TRUE, FALSE, TQBF }
	
	// constructors
	public DTNode(DTNodeType type) {
		this.type = type;
	}
	
	synchronized public String dump() {
		
		String indent = "";
		for(int i = 0; i < this.depth; i++)
			indent += "  ";
		
		String s = indent + this + "\n";
		if(leftChild != null)
			s += leftChild.dump();
		if(rightChild != null)
			s += rightChild.dump();
		return s;
	}
	
	public String toString() {
		if(type.equals(DTNodeType.TQBF)) {
			return type.toString() + "(" + this.getId() + ")" + " " + variablesAssignedTrue + ", " + variablesAssignedFalse;
		}
		return type.toString() + " " + variablesAssignedTrue + ", " + variablesAssignedFalse;
	}
	
	synchronized public int getDepth() {
		if(depth >= 0)
			return depth;
		if(parent == null) {
			depth = 0;
			return 0;
		}
		depth = parent.getDepth()+1;
		return depth;			
	}
	
	public synchronized void reduce() {
		if(parent == null)
			return;
		DTNode sibling = null;
		
		if (getParent().getRightChild() != this) {
			sibling = getParent().getRightChild();
		} else {
			sibling = getParent().getLeftChild();
		}
		
		if(getParent().getType() == DTNodeType.AND) {
			// AND FALSE
			if (this.type == DTNodeType.FALSE) {
				parent.setType(DTNodeType.FALSE);
				parent.setLeftChild(null);
				parent.setRightChild(null);
				sibling.setParent(null);
				sibling.abortLeafNodes();
			// AND TRUE
			} else if(this.type == DTNodeType.TRUE) {
				if (sibling.hasTruthValue()) {
					parent.setTruthValue(sibling.getTruthValue() && this.getTruthValue());
					parent.setLeftChild(null);
					parent.setRightChild(null);
					sibling.setParent(null);
				}
			}
		} else if(getParent().getType() == DTNodeType.OR) {
			// OR TRUE
			if(this.type == DTNodeType.TRUE) {
				parent.setTruthValue(true);
				parent.setLeftChild(null);
				parent.setRightChild(null);
				sibling.setParent(null);
				sibling.abortLeafNodes();
			// OR FALSE
			} else if(this.type == DTNodeType.FALSE) {
				if (sibling.hasTruthValue()) {
					parent.setTruthValue(sibling.getTruthValue() || this.getTruthValue());
					parent.setLeftChild(null);
					parent.setRightChild(null);
					sibling.setParent(null);
				}
			}
		} else {
			logger.error(String.format("Unexpected parent-DTNodeType: %s", getParent().getType()));
			throw new IllegalStateException(String.format("Unexpected parent-DTNodeType: %s", getParent().getType()));
		}
//logger.info("reduced to " + this);
		parent.reduce();
	}

	public synchronized DTNode getNode(String tqbfId) {
		DTNode tmpLeft = null;
		DTNode tmpRight = null;
		if (this.id != null && this.id.equals(tqbfId)) {
			return this;
		}
	
		if (leftChild != null) tmpLeft = leftChild.getNode(tqbfId);
		if (rightChild != null) tmpRight = rightChild.getNode(tqbfId);

		if (tmpLeft != null) return tmpLeft;
		return tmpRight;


	}
	
	public ArrayList<DTNode> getLeafNodes() {
		ArrayList<DTNode> leafNodes = new ArrayList<DTNode>();

		// if there are no childs, we return the node
		if ((leftChild == null) && (rightChild == null)) {
			leafNodes.add(this);
			return leafNodes;
		}

		// else we continue our search
		leafNodes.addAll(leftChild.getLeafNodes());
		leafNodes.addAll(rightChild.getLeafNodes());

		return leafNodes;
	}
	
	public void abortLeafNodes() {
		if ((leftChild == null) && (rightChild == null) && this.type == DTNodeType.TQBF) {
			TQbf.tqbfs.get(this.id).abort();
			return;
		}
		if(leftChild != null)
			leftChild.getLeafNodes();
		if(rightChild != null)
			rightChild.getLeafNodes();
	}

	public void addChild(DTNode n) {
		if (this.leftChild == null) {
			n.setParent(this);
			this.leftChild = n;
		}
		else if (this.rightChild == null) {
			n.setParent(this);
			this.rightChild = n;
		}
	}

	// getter & setter
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setParent(DTNode p) {
		this.parent = p;
	}
	
	public DTNode getParent() {
		return this.parent;
	}
	
	public void setLeftChild(DTNode c) {
		this.leftChild = c;
	}
	
	public DTNode getLeftChild() {
		return this.leftChild;
	}
	
	public void setRightChild(DTNode c) {
		this.rightChild = c;
	}
	
	public DTNode getRightChild() {
		return this.rightChild;
	}
		
	public boolean hasTruthValue () {
		if(this.type == DTNodeType.TRUE || this.type == DTNodeType.FALSE)
			return true;
		return false;
	}
	
	public void setType(DTNodeType type) {
		this.type = type;
	}	

	public DTNodeType getType() {
		return this.type;
	}

	public void setTruthValue(boolean result) {
		if(result)
			this.setType(DTNodeType.TRUE);
		else
			this.setType(DTNodeType.FALSE);
	}

	public boolean getTruthValue() {
		if(this.type == DTNodeType.TRUE)
			return true;
		else if((this.type == DTNodeType.FALSE))
			return false;
		else
			assert(false);
		throw new RuntimeException();
	}
}
