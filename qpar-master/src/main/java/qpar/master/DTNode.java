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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// a simple node class
public class DTNode {

	public enum DTNodeType {
		AND, OR, TRUE, FALSE, TQBF
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DTNode.class);
	private String id = null;
	private DTNode leftChild = null;
	private DTNode rightChild = null;
	private DTNode parent = null;
	private int depth = -1;
	public DTNodeType type;
	public ArrayList<Integer> variablesAssignedTrue = new ArrayList<Integer>();

	public ArrayList<Integer> variablesAssignedFalse = new ArrayList<Integer>();

	// constructors
	public DTNode(final DTNodeType type) {
		this.type = type;
	}

	public void abortLeafNodes() {
		if ((this.leftChild == null) && (this.rightChild == null) && this.type == DTNodeType.TQBF) {
			TQbf.tqbfs.get(this.id).abort();
			return;
		}
		if (this.leftChild != null) {
			this.leftChild.getLeafNodes();
		}
		if (this.rightChild != null) {
			this.rightChild.getLeafNodes();
		}
	}

	public void addChild(final DTNode n) {
		if (this.leftChild == null) {
			n.setParent(this);
			this.leftChild = n;
		} else if (this.rightChild == null) {
			n.setParent(this);
			this.rightChild = n;
		}
	}

	synchronized public String dump() {

		String indent = "";
		for (int i = 0; i < this.depth; i++) {
			indent += "  ";
		}

		String s = indent + this + "\n";
		if (this.leftChild != null) {
			s += this.leftChild.dump();
		}
		if (this.rightChild != null) {
			s += this.rightChild.dump();
		}
		return s;
	}

	synchronized public int getDepth() {
		if (this.depth >= 0) {
			return this.depth;
		}
		if (this.parent == null) {
			this.depth = 0;
			return 0;
		}
		this.depth = this.parent.getDepth() + 1;
		return this.depth;
	}

	// getter & setter
	public String getId() {
		return this.id;
	}

	public ArrayList<DTNode> getLeafNodes() {
		ArrayList<DTNode> leafNodes = new ArrayList<DTNode>();

		// if there are no childs, we return the node
		if ((this.leftChild == null) && (this.rightChild == null)) {
			leafNodes.add(this);
			return leafNodes;
		}

		// else we continue our search
		leafNodes.addAll(this.leftChild.getLeafNodes());
		leafNodes.addAll(this.rightChild.getLeafNodes());

		return leafNodes;
	}

	public DTNode getLeftChild() {
		return this.leftChild;
	}

	public synchronized DTNode getNode(final String tqbfId) {
		DTNode tmpLeft = null;
		DTNode tmpRight = null;
		if (this.id != null && this.id.equals(tqbfId)) {
			return this;
		}

		if (this.leftChild != null) {
			tmpLeft = this.leftChild.getNode(tqbfId);
		}
		if (this.rightChild != null) {
			tmpRight = this.rightChild.getNode(tqbfId);
		}

		if (tmpLeft != null) {
			return tmpLeft;
		}
		return tmpRight;

	}

	public DTNode getParent() {
		return this.parent;
	}

	public DTNode getRightChild() {
		return this.rightChild;
	}

	public boolean getTruthValue() {
		if (this.type == DTNodeType.TRUE) {
			return true;
		} else if ((this.type == DTNodeType.FALSE)) {
			return false;
		} else {
			assert (false);
		}
		throw new RuntimeException();
	}

	public DTNodeType getType() {
		return this.type;
	}

	public boolean hasTruthValue() {
		if (this.type == DTNodeType.TRUE || this.type == DTNodeType.FALSE) {
			return true;
		}
		return false;
	}

	public synchronized void reduce() {
		if (this.parent == null) {
			return;
		}
		DTNode sibling = null;

		if (this.getParent().getRightChild() != this) {
			sibling = this.getParent().getRightChild();
		} else {
			sibling = this.getParent().getLeftChild();
		}

		if (this.getParent().getType() == DTNodeType.AND) {
			// AND FALSE
			if (this.type == DTNodeType.FALSE) {
				this.parent.setType(DTNodeType.FALSE);
				this.parent.setLeftChild(null);
				this.parent.setRightChild(null);
				sibling.setParent(null);
				sibling.abortLeafNodes();
				// AND TRUE
			} else if (this.type == DTNodeType.TRUE) {
				if (sibling.hasTruthValue()) {
					this.parent.setTruthValue(sibling.getTruthValue() && this.getTruthValue());
					this.parent.setLeftChild(null);
					this.parent.setRightChild(null);
					sibling.setParent(null);
				}
			}
		} else if (this.getParent().getType() == DTNodeType.OR) {
			// OR TRUE
			if (this.type == DTNodeType.TRUE) {
				this.parent.setTruthValue(true);
				this.parent.setLeftChild(null);
				this.parent.setRightChild(null);
				sibling.setParent(null);
				sibling.abortLeafNodes();
				// OR FALSE
			} else if (this.type == DTNodeType.FALSE) {
				if (sibling.hasTruthValue()) {
					this.parent.setTruthValue(sibling.getTruthValue() || this.getTruthValue());
					this.parent.setLeftChild(null);
					this.parent.setRightChild(null);
					sibling.setParent(null);
				}
			}
		} else {
			LOGGER.error(String.format("Unexpected parent-DTNodeType: %s", this.getParent().getType()));
			throw new IllegalStateException(String.format("Unexpected parent-DTNodeType: %s", this.getParent().getType()));
		}
		// logger.info("reduced to " + this);
		this.parent.reduce();
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setLeftChild(final DTNode c) {
		this.leftChild = c;
	}

	public void setParent(final DTNode p) {
		this.parent = p;
	}

	public void setRightChild(final DTNode c) {
		this.rightChild = c;
	}

	public void setTruthValue(final boolean result) {
		if (result) {
			this.setType(DTNodeType.TRUE);
		} else {
			this.setType(DTNodeType.FALSE);
		}
	}

	public void setType(final DTNodeType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		if (this.type.equals(DTNodeType.TQBF)) {
			return this.type.toString() + "(" + this.getId() + ")" + " " + this.variablesAssignedTrue + ", " + this.variablesAssignedFalse;
		}
		return this.type.toString() + " " + this.variablesAssignedTrue + ", " + this.variablesAssignedFalse;
	}
}
