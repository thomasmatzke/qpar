package main.java.logic;

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

	public DTNode getNode(int id) {
		if (this.id == id) {
			return this;
		}
		DTNode tmp;
		tmp = this.leftChild.getNode(id);
		if (tmp.getId() == id)
			return tmp;
		tmp = this.rightChild.getNode(id);
		if (tmp.getId() == id)
			return tmp;
		return null;
	}
	
	public void addChild(DTNode n) {
		if (this.leftChild == null) {
			this.leftChild = n;
		}
		else if (this.rightChild == null) {
			this.rightChild = n;
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
