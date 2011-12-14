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

import org.apache.log4j.Logger;

import qpar.master.logic.parser.Node;
import qpar.master.logic.parser.SimpleNode;
import qpar.master.logic.parser.SimpleNode.NodeType;

public class QproRepresentation {
	static Logger logger = Logger.getLogger(QproRepresentation.class);

	SimpleNode root;
	
	public QproRepresentation(ReducedInterpretation r) {
		this.root = r.getInterpretation();
	}
	
	public String getQproRepresentation() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("QBF\n" + ((Collections.max(root.getVariableSet()))+1) + "\n");
				
		if(root.isStartNode()) {
			buf.append(traverse((SimpleNode)root.children[0]));
		} else {
			buf.append(traverse((SimpleNode)root));
		}
				
		buf.append("QBF\n");
		return buf.toString();
	}
			
	public String traverse(SimpleNode n) {
//		logger.info("Traversing visited: " + n.toString());
		StringBuffer traversedTree = new StringBuffer();
		Set<Integer> posLiterals = new HashSet<Integer>();
		Set<Integer> negLiterals = new HashSet<Integer>();
		SimpleNode tmpNode = null;
		
		SimpleNode parent = (SimpleNode) n.jjtGetParent();
		
		if (n.isExistsNode()) {
			assert(n.jjtGetNumChildren() == 1);
			
			if(!parent.isForallNode())
				traversedTree.append("q\n");
			traversedTree.append("e ");

			// add the first var
			traversedTree.append((n.var+1) + " ");
			
			tmpNode = (SimpleNode)n.jjtGetChild(0);

			while (tmpNode.isExistsNode()) {
				traversedTree.append((tmpNode.getVar()+1) + " ");
				tmpNode = (SimpleNode)tmpNode.jjtGetChild(0);
			}
			traversedTree.append("\n");
			traversedTree.append(traverse(tmpNode));
			if (!((SimpleNode)n.jjtGetParent()).isQuantifierNode())
				traversedTree.append("/q\n");
			
		}	
			
		if (n.isForallNode()) {	
			assert(n.jjtGetNumChildren() == 1);
			
			if(!parent.isExistsNode())
				traversedTree.append("q\n");
			traversedTree.append("a ");

			// add the first var
			traversedTree.append((n.var+1) + " ");
			
			tmpNode = (SimpleNode)n.jjtGetChild(0);

			while (tmpNode.isForallNode()) {
				traversedTree.append((tmpNode.getVar()+1) + " ");
				tmpNode = (SimpleNode)tmpNode.jjtGetChild(0);
			}
			traversedTree.append("\n");
			traversedTree.append(traverse(tmpNode));
//			if(!parent.isQuantifierNode())
//				traversedTree.append("/q\n");
			if (!((SimpleNode)n.jjtGetParent()).isQuantifierNode())
				traversedTree.append("/q\n");
		}	
			
		if (n.isAndNode()) {
			assert(n.jjtGetNumChildren() == 2);
			
			/*
			 *if ((jjtGetParent().getNodeType() == NodeType.FORALL) || (jjtGetParent().getNodeType() == NodeType.EXISTS))
			 *    traversedTree += "\n";
			 */
			traversedTree.append("c\n");
			posLiterals = (n.getPositiveLiterals(NodeType.AND, posLiterals));
			negLiterals = (n.getNegativeLiterals(NodeType.AND, negLiterals));

			for (int var : posLiterals)
				traversedTree.append(" " + (var+1));
			traversedTree.append(" \n");

			for (int var : negLiterals)
				traversedTree.append(" " + (var+1));
			traversedTree.append(" \n");

			traversedTree.append(getEnclosedFormula((SimpleNode)n, NodeType.OR));

			traversedTree.append("/c\n");
		}

		if (n.isOrNode()) {
			assert(n.jjtGetNumChildren() == 2);
			/*
			 *if ((jjtGetParent().getNodeType() == NodeType.FORALL) || (jjtGetParent().getNodeType() == NodeType.EXISTS))
			 *    traversedTree += "\n";
			 */
			traversedTree.append("d\n");
			posLiterals = (n.getPositiveLiterals(NodeType.OR, posLiterals));
			negLiterals = (n.getNegativeLiterals(NodeType.OR, negLiterals));

			for (int var : posLiterals)
				traversedTree.append(" " + (var+1));
			traversedTree.append(" \n");

			for (int var : negLiterals)
				traversedTree.append(" " + (var+1));
			traversedTree.append(" \n");

			traversedTree.append(getEnclosedFormula((SimpleNode)n, NodeType.AND));

			traversedTree.append("/d\n");
		}
		
		if(n.isVarNode()) {
			traversedTree.append(String.format("c\n%s\n\n/c\n", n.getNodeVariable()+1));
		}
		
		if(n.isNotNode() && ((SimpleNode)n.jjtGetChild(0)).isVarNode()) {
			traversedTree.append(String.format("c\n\n%s\n/c\n", n.getNodeVariable()+1));
		}

		return traversedTree.toString();
	}
	
	public String getEnclosedFormula(SimpleNode n, NodeType op) {
		if(n.jjtGetNumChildren() == 0)
			return "";
		
		StringBuffer tmp = new StringBuffer();
		for(Node node : n.children) {
			assert(node !=  null);
			SimpleNode child = (SimpleNode) node;
			if (child.getNodeType().equals(op) || child.isQuantifierNode()) {
				tmp.append(traverse(child));
			} else {
				tmp.append(getEnclosedFormula(child, op));
			}
		}

		return tmp.toString();
	}
}
