package main.java.logic;

import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

// a simple node class
public class DTNode {

    static Logger logger = Logger.getLogger(Qbf.class);
	private int id = -1;
	private boolean hasTruthValue = false;
	private boolean truthValue;
	private String op = "";
	private DTNode leftChild = null;
	private DTNode rightChild = null;
	private DTNode parent = null;

	// constructors
	public DTNode(String op) {
		this.op = op;
	}

	public DTNode(int id) {
		this.id = id;
	}

	public DTNode() {
	}

	
	public void reduce() {
		String op = getParent().getOp();
		DTNode sibling = null;

		if (getParent().getRightChild() != this) {
			sibling = getParent().getRightChild();
		} else {
			sibling = getParent().getLeftChild();
		}


		if (op.equals("&")) {
			// AND FALSE
			if (truthValue == false) {
				parent.setTruthValue(false);
				parent.setOp("");
				parent.setLeftChild(null);
				parent.setRightChild(null);
				parent = null;
				sibling.setParent(null);
			// AND TRUE
			} else if (truthValue == true) {
				if (sibling.hasTruthValue) {
					parent.setTruthValue(sibling.getTruthValue() && truthValue);
					parent.setOp("");
					parent.setLeftChild(null);
					parent.setRightChild(null);
					parent = null;
					sibling.setParent(null);
				}
			}
		} else if (op.equals("|")) {
			// OR TRUE
			if (truthValue == true) {
				parent.setTruthValue(true);
				parent.setOp("");
				parent.setLeftChild(null);
				parent.setRightChild(null);
				parent = null;
				sibling.setParent(null);
			// OR FALSE
			} else if (truthValue == false) {
				if (sibling.hasTruthValue) {
					parent.setTruthValue(sibling.getTruthValue() || truthValue);
					parent.setOp("");
					parent.setLeftChild(null);
					parent.setRightChild(null);
					parent = null;
					sibling.setParent(null);
				}
			}
		}
	}

	public DTNode getNode(int sid) {
		DTNode tmpLeft = null;
		DTNode tmpRight = null;
		if (this.id == sid) {
			return this;
		}
	
		if (leftChild != null) tmpLeft = leftChild.getNode(sid);
		if (rightChild != null) tmpRight = rightChild.getNode(sid);

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

	public void addLayer(String op) {
		DTNode tmp = null;
		if (leftChild == null) {
			tmp = new DTNode(op);
			tmp.setParent(this);	
			leftChild = tmp;
		} else {
			leftChild.addLayer(op);
		}

		if (rightChild == null) {
			tmp = new DTNode(op);
			tmp.setParent(this);
			rightChild = tmp;
		} else {
			rightChild.addLayer(op);
		}
	}

	// getter & setter
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
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
	
	public void setTruthValue (boolean t) {
		this.hasTruthValue = true;
		this.truthValue = t;
	}

	public boolean getTruthValue () {
		return this.truthValue;
	}
	
	public boolean hasTruthValue () {
		return this.hasTruthValue;
	}
	
	public void setOp(String op) {
		this.op = op;
	}	

	public String getOp() {
		return this.op;
	}
}
