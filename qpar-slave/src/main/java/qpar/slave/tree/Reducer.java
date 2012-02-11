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
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.parser.jjtree.Node;
import qpar.common.parser.jjtree.SimpleNode;

/**
 * Reduces a tree of Nodes following boolean rules
 * 
 * @author thomasm
 * 
 */
public class Reducer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Reducer.class);

	ArrayDeque<Node> reducableNodes;

	public Reducer(final ArrayDeque<Node> reducableNodes) {
		this.reducableNodes = reducableNodes;
	}

	public void reduce() {
		for (Node node : this.reducableNodes) {
			if (!node.checkConnectionToRoot()) {
				continue;
			}

			Node current = node;
			Node newNode = null;

			do {
				LOGGER.trace("Reducing node " + current + " with children " + Arrays.toString(current.getChildren()));

				// if(((Node)current).isVarNode() &&
				// ((Node)current).getNodeVariable() == 1127)
				// current.jjtGetParent().dump(Thread.currentThread().getName());

				if (current.jjtGetNumChildren() == 2) {
					assert (current.jjtGetChild(0) != null);
					assert (current.jjtGetChild(1) != null);
					newNode = this.evaluate(current, current.jjtGetChild(0), current.jjtGetChild(1));
				} else if (current.jjtGetNumChildren() == 1) {
					if (!(current.isNotNode() || current.isQuantifierNode() || current.isStartNode())) {
						LOGGER.error(String.format("Node of type: %s, but has 1 child", current.getNodeType()));
						throw new IllegalStateException();
					}
					newNode = this.evaluate(current, current.jjtGetChild(0), null);
				} else {
					throw new RuntimeException("Node must have 1 or 2 children. Has: " + current.jjtGetNumChildren());
				}

				newNode.jjtSetParent(current.jjtGetParent());
				for (int i = 0; i < current.jjtGetParent().jjtGetNumChildren(); i++) {
					if (current.jjtGetParent().jjtGetChild(i) == current) {
						current.jjtGetParent().jjtAddChild(newNode, i);
					}
				}
				LOGGER.trace("Reduced to: " + newNode);

				// if(((Node)current.jjtGetParent()).isExistsNode() &&
				// ((Node)current.jjtGetParent()).getNodeVariable() ==
				// 1127)
				// current.jjtGetParent().dump(Thread.currentThread().getName());

				current = newNode.jjtGetParent();
			} while (newNode.isTruthNode() && !current.isStartNode());

		}

	}

	/**
	 * Gets the new node resulting from boolean resolution left OR right MUST be
	 * TRUE or FALSE
	 * 
	 * @param left
	 * @param right
	 *            Set to null if there only is one child
	 * @return
	 */
	private Node evaluate(final Node parent, final Node left, final Node right) {
		Node newNode = new SimpleNode();

		// Panic if none of the children are truth-nodes
		if (!left.isTruthNode() && right != null && !right.isTruthNode()) {
			throw new RuntimeException();
		}

		// Handle case where both children are truth nodes
		if (left.isTruthNode() && right != null && right.isTruthNode()) {
			switch (parent.getNodeType()) {
			case AND:
				newNode.setTruthValue(left.getTruth() && right.getTruth());
				return newNode;
			case OR:
				newNode.setTruthValue(left.getTruth() || right.getTruth());
				return newNode;
			default:
				throw new RuntimeException();
			}
		}

		// handle rest of possibilities
		switch (parent.getNodeType()) {
		case FORALL:
		case EXISTS:
			assert (right == null);
			assert (left.isTruthNode());
			newNode.setTruthValue(left.getTruth());
			break;
		case NOT:
			assert (parent.isNotNode());
			assert (right == null);
			assert (left.isTruthNode());
			// logger.info("Left type before: " + left.getNodeType());
			newNode.setTruthValue(!left.getTruth());
			// logger.info("Left type after: " + left.getNodeType());
			break;
		case AND:
			assert (parent.isAndNode());
			// logger.debug(String.format("parent: %s, left: %s, right: %s",
			// parent.getNodeType(), left.getNodeType(), right.getNodeType()));
			if (left.isTruthNode()) {
				return this.evaluate(parent, right, left);
			}
			if (right.getTruth()) {
				newNode = left;
			} else {
				newNode.setTruthValue(false);
			}
			break;
		case OR:
			assert (parent.isOrNode());
			if (left.isTruthNode()) {
				return this.evaluate(parent, right, left);
			}
			if (!right.getTruth()) {
				newNode = left;
			} else {
				newNode.setTruthValue(true);
			}
			break;
		case VAR:
		case FALSE:
		case TRUE:
			throw new IllegalStateException();
		default:
			throw new IllegalStateException();
		}

		assert (newNode != null);

		return newNode;

	}

}
