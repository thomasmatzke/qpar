package main.java.logic;

import java.util.ArrayList;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

// a simple node class
public class DTNode {

    static Logger logger = Logger.getLogger(Qbf.class);
	private String id = null;
	private DTNode leftChild = null;
	private DTNode rightChild = null;
	private DTNode parent = null;
	private int depth = 0;
	public DTNodeType type;
	public ArrayList<Integer> variablesAssignedTrue 	= new ArrayList<Integer>();
	public ArrayList<Integer> variablesAssignedFalse 	= new ArrayList<Integer>();
	
	public enum DTNodeType { AND, OR, TRUE, FALSE, UNDEFINED }
	
	// constructors
	public DTNode(DTNodeType type) {
		this.type = type;
	}
	
	public int getDepth() {
		if(depth != 0)
			return depth;
		DTNode p = parent;
		int d = 0;
		while(p != null) {
			d++;
		}
		return d;
	}
	
	public synchronized void reduce() {
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
				parent = null;
				sibling.setParent(null);
				if(this.parent != null)
					parent.reduce();
			// AND TRUE
			} else if(this.type == DTNodeType.TRUE) {
				if (sibling.hasTruthValue()) {
					parent.setTruthValue(sibling.getTruthValue() && this.getTruthValue());
					parent.setLeftChild(null);
					parent.setRightChild(null);
					parent = null;
					sibling.setParent(null);
					if(this.parent != null)
						parent.reduce();
				}
			}
		} else if(getParent().getType() == DTNodeType.OR) {
			// OR TRUE
			if(this.type == DTNodeType.TRUE) {
				parent.setTruthValue(true);
				parent.setLeftChild(null);
				parent.setRightChild(null);
				parent = null;
				sibling.setParent(null);
				if(this.parent != null)
					parent.reduce();
			// OR FALSE
			} else if(this.type == DTNodeType.FALSE) {
				if (sibling.hasTruthValue()) {
					parent.setTruthValue(sibling.getTruthValue() || this.getTruthValue());
					parent.setLeftChild(null);
					parent.setRightChild(null);
					parent = null;
					sibling.setParent(null);
					if(this.parent != null)
						parent.reduce();
				}
			}
		}
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
	
	public Vector<DTNode> getLeafNodes() {
		Vector<DTNode> leafNodes = new Vector<DTNode>();

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

	public void addLayer(DTNodeType operator) {
		assert(operator == DTNodeType.AND || operator == DTNodeType.OR);
		DTNode tmp = null;
		if (leftChild == null) {
			tmp = new DTNode(operator);
			tmp.setParent(this);	
			leftChild = tmp;
		} else {
			leftChild.addLayer(operator);
		}

		if (rightChild == null) {
			tmp = new DTNode(operator);
			tmp.setParent(this);
			rightChild = tmp;
		} else {
			rightChild.addLayer(operator);
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
			this.setType(DTNode.DTNodeType.TRUE);
		else
			this.setType(DTNode.DTNodeType.FALSE);
	}

	public boolean getTruthValue() {
		if(this.type == DTNode.DTNodeType.TRUE)
			return true;
		else if((this.type == DTNode.DTNodeType.FALSE))
			return false;
		else
			assert(false);
		return false;
	}
}
