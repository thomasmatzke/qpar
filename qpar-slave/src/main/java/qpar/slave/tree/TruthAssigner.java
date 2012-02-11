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
package qpar.slave.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.parser.jjtree.Node;
import qpar.common.parser.jjtree.Node.NodeType;

/**
 * Takes a SimpleNode and assigns truth values to variables in the subtree
 * 
 * @author thomasm
 * 
 */
public class TruthAssigner {

	private static final Logger LOGGER = LoggerFactory.getLogger(TruthAssigner.class);

	Node root;
	ArrayList<Integer> trueVars;
	ArrayList<Integer> falseVars;

	public TruthAssigner(final Node root, final ArrayList<Integer> trueVars, final ArrayList<Integer> falseVars) {
		this.root = root;
		// logger.info("new TruthAssigner. rootNode: " + this.root.hashCode());
		this.trueVars = trueVars;
		this.falseVars = falseVars;
	}

	/**
	 * Assigns nodes and returns them
	 * 
	 * @return
	 */
	public ArrayDeque<Node> assign() {
		LOGGER.debug("Assigning " + Arrays.toString(this.trueVars.toArray()) + ", " + Arrays.toString(this.falseVars.toArray()));
		ArrayDeque<Node> assigned = this.assignNode(this.root);
		LOGGER.debug("Assigned " + assigned.size() + " nodes.");

		return assigned;
	}

	private ArrayDeque<Node> assignNode(final Node node) {
		ArrayDeque<Node> ret = new ArrayDeque<Node>();
		if (node.getNodeType().equals(NodeType.VAR)) {
			if (this.trueVars.contains(node.getVar())) {
				node.setNodeType(NodeType.TRUE);
				ret.add(node);
			} else if (this.falseVars.contains(node.getVar())) {
				node.setNodeType(NodeType.FALSE);
				ret.add(node);
			}

		} else {
			for (Node n : node.getChildren()) {
				ret.addAll(this.assignNode(n));
			}
		}
		return ret;
	}

}
