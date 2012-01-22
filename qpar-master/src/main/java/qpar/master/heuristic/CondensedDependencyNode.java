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
package qpar.master.heuristic;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.master.heuristic.DependencyNode.NodeType;

/**
 * Used by getDecisionGroups
 * 
 * @author thomasm
 * 
 */
public class CondensedDependencyNode {
	// TODO: make this DRY -> GenericNode with common stuff from Condensed and
	// DependencyNode
	private static final Logger LOGGER = LoggerFactory.getLogger(CondensedDependencyNode.class);

	private Integer depth;
	public NodeType type;
	public ArrayList<Integer> variables = new ArrayList<Integer>();
	private final ArrayList<CondensedDependencyNode> children = new ArrayList<CondensedDependencyNode>();
	private CondensedDependencyNode parent = null;

	public CondensedDependencyNode(final DependencyNode.NodeType t) {
		this.type = t;
		if (this.type == NodeType.ROOT) {
			this.setDepth(0);
		}
	}

	public Integer getDepth() {
		if (this.depth == null) {
			this.depth = this.parent.getDepth() + 1;
		}
		return this.depth;
	}

	public final void setDepth(final Integer depth) {
		this.depth = depth;
		for (CondensedDependencyNode d : this.children) {
			d.setDepth(depth + 1);
		}
	}

	public ArrayList<CondensedDependencyNode> getChildren() {
		return this.children;
	}

	public void addChild(final CondensedDependencyNode n) {
		if (this.depth != null) {
			n.depth = this.depth + 1;
		}
		this.children.add(n);
		n.setParent(this);
	}

	public String dump() {

		String indent = "";
		for (int i = 0; i < this.getDepth(); i++) {
			indent += "  ";
		}

		String s = indent + this + "\n";
		switch (this.children.size()) {
		case 0:
			return s;
		case 1:
			s += this.children.get(0).dump();
			break;
		case 2:
			s += this.children.get(0).dump();
			s += this.children.get(1).dump();
			break;
		default:
			assert (false);

		}
		return s;

	}

	@Override
	public String toString() {
		return this.type.toString() + "(" + this.variables.toString() + ", depth: " + this.depth + ")";
	}

	/**
	 * Returns all subnodes, including this
	 * 
	 * @return Subnodes of this
	 */
	public ArrayList<CondensedDependencyNode> allSubnodes() {
		switch (this.children.size()) {
		case 0:
			ArrayList<CondensedDependencyNode> n0 = new ArrayList<CondensedDependencyNode>();
			n0.add(this);
			return n0;
		case 1:
			ArrayList<CondensedDependencyNode> n1 = this.children.get(0).allSubnodes();
			n1.add(this);
			return n1;
		case 2:
			ArrayList<CondensedDependencyNode> n20 = this.children.get(0).allSubnodes();
			ArrayList<CondensedDependencyNode> n21 = this.children.get(1).allSubnodes();
			n20.addAll(n21);
			n20.add(this);
			return n20;
		default:
			LOGGER.error("A DependencyNode must not have more than 3 childnodes.");
			System.exit(-1);
			return null;
		}
	}

	public void setParent(final CondensedDependencyNode parent) {
		this.parent = parent;
	}

	public CondensedDependencyNode getParent() {
		return this.parent;
	}

}