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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import qpar.common.parser.jjtree.Node;
import qpar.common.parser.jjtree.Node.NodeType;

public class QproRepresentation {

	Node root;

	public QproRepresentation(final ReducedInterpretation r) {
		this.root = r.getInterpretation();
	}

	public String getQproRepresentation() {
		StringBuffer buf = new StringBuffer();

		buf.append("QBF\n" + ((Collections.max(this.root.getVariableSet())) + 1) + "\n");

		if (this.root.isStartNode()) {
			buf.append(this.traverse(this.root.getChildren()[0]));
		} else {
			buf.append(this.traverse(this.root));
		}

		buf.append("QBF\n");
		return buf.toString();
	}

	public String traverse(final Node n) {
		StringBuffer traversedTree = new StringBuffer();
		Set<Integer> posLiterals = new HashSet<Integer>();
		Set<Integer> negLiterals = new HashSet<Integer>();
		Node tmpNode = null;

		Node parent = n.jjtGetParent();

		if (n.isExistsNode()) {
			assert (n.jjtGetNumChildren() == 1);

			if (!parent.isForallNode()) {
				traversedTree.append("q\n");
			}
			traversedTree.append("e ");

			// add the first var
			traversedTree.append((n.getVar() + 1) + " ");

			tmpNode = n.jjtGetChild(0);

			while (tmpNode.isExistsNode()) {
				traversedTree.append((tmpNode.getVar() + 1) + " ");
				tmpNode = tmpNode.jjtGetChild(0);
			}
			traversedTree.append("\n");
			traversedTree.append(this.traverse(tmpNode));
			if (!n.jjtGetParent().isQuantifierNode()) {
				traversedTree.append("/q\n");
			}

		}

		if (n.isForallNode()) {
			assert (n.jjtGetNumChildren() == 1);

			if (!parent.isExistsNode()) {
				traversedTree.append("q\n");
			}
			traversedTree.append("a ");

			// add the first var
			traversedTree.append((n.getVar() + 1) + " ");

			tmpNode = n.jjtGetChild(0);

			while (tmpNode.isForallNode()) {
				traversedTree.append((tmpNode.getVar() + 1) + " ");
				tmpNode = tmpNode.jjtGetChild(0);
			}
			traversedTree.append("\n");
			traversedTree.append(this.traverse(tmpNode));
			// if(!parent.isQuantifierNode())
			// traversedTree.append("/q\n");
			if (!n.jjtGetParent().isQuantifierNode()) {
				traversedTree.append("/q\n");
			}
		}

		if (n.isAndNode()) {
			assert (n.jjtGetNumChildren() == 2);

			/*
			 * if ((jjtGetParent().getNodeType() == NodeType.FORALL) ||
			 * (jjtGetParent().getNodeType() == NodeType.EXISTS)) traversedTree
			 * += "\n";
			 */
			traversedTree.append("c\n");
			posLiterals = (n.getPositiveLiterals(NodeType.AND, posLiterals));
			negLiterals = (n.getNegativeLiterals(NodeType.AND, negLiterals));

			for (int var : posLiterals) {
				traversedTree.append(" " + (var + 1));
			}
			traversedTree.append(" \n");

			for (int var : negLiterals) {
				traversedTree.append(" " + (var + 1));
			}
			traversedTree.append(" \n");

			traversedTree.append(this.getEnclosedFormula(n, NodeType.OR));

			traversedTree.append("/c\n");
		}

		if (n.isOrNode()) {
			assert (n.jjtGetNumChildren() == 2);
			/*
			 * if ((jjtGetParent().getNodeType() == NodeType.FORALL) ||
			 * (jjtGetParent().getNodeType() == NodeType.EXISTS)) traversedTree
			 * += "\n";
			 */
			traversedTree.append("d\n");
			posLiterals = (n.getPositiveLiterals(NodeType.OR, posLiterals));
			negLiterals = (n.getNegativeLiterals(NodeType.OR, negLiterals));

			for (int var : posLiterals) {
				traversedTree.append(" " + (var + 1));
			}
			traversedTree.append(" \n");

			for (int var : negLiterals) {
				traversedTree.append(" " + (var + 1));
			}
			traversedTree.append(" \n");

			traversedTree.append(this.getEnclosedFormula(n, NodeType.AND));

			traversedTree.append("/d\n");
		}

		if (n.isVarNode()) {
			traversedTree.append(String.format("c\n%s\n\n/c\n", n.getVar() + 1));
		}

		if (n.isNotNode() && n.jjtGetChild(0).isVarNode()) {
			traversedTree.append(String.format("c\n\n%s\n/c\n", n.getVar() + 1));
		}

		return traversedTree.toString();
	}

	public String getEnclosedFormula(final Node n, final NodeType op) {
		if (n.jjtGetNumChildren() == 0) {
			return "";
		}

		StringBuffer tmp = new StringBuffer();
		for (Node node : n.getChildren()) {
			assert (node != null);
			Node child = node;
			if (child.getNodeType().equals(op) || child.isQuantifierNode()) {
				tmp.append(this.traverse(child));
			} else {
				tmp.append(this.getEnclosedFormula(child, op));
			}
		}

		return tmp.toString();
	}
}
