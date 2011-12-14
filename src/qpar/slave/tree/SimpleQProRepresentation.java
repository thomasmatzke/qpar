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

import org.apache.log4j.Logger;

import qpar.master.logic.parser.SimpleNode;

public class SimpleQProRepresentation {
	static Logger logger = Logger.getLogger(QproRepresentation.class);

	SimpleNode root;
	String representation = null;
	
	
	public SimpleQProRepresentation(ReducedInterpretation r) {
		this.root = r.getInterpretation();
	}
	
	public String getQproRepresentation() {
		if(representation == null)
			representation = generate(root);
		
		return representation;
	}
	
	private String generate(SimpleNode n) {
		String left=null, right=null, whole=null;
		
		String containerFormat = null;
		if(n.getNodeType() != SimpleNode.NodeType.START)
			containerFormat = getPseudoConnectiveFormatString((SimpleNode)n.jjtGetParent());
		
		switch(n.getNodeType()) {
			case START:
				return String.format("QBF\n%d\n%sQBF", ((Collections.max(root.getVariableSet()))+1), generate((SimpleNode)n.jjtGetChild(0)));
			case FORALL:
				String forallString = "q\na %s\n%s/q\n";
				if(((SimpleNode)n.jjtGetChild(0)).isAndNode()  || ((SimpleNode)n.jjtGetChild(0)).isOrNode()) {
					return String.format(forallString, n.getNodeVariable()+1, generate((SimpleNode)n.jjtGetChild(0)));
				}
				forallString = "q\na %s\nc\n\n\n%s/c\n/q\n";				
				return String.format(forallString, n.getNodeVariable()+1, generate((SimpleNode)n.jjtGetChild(0)));
			case EXISTS:				
				String existsString = "q\ne %s\n%s/q\n";
				if(((SimpleNode)n.jjtGetChild(0)).isAndNode()  || ((SimpleNode)n.jjtGetChild(0)).isOrNode()) {
					return String.format(existsString, n.getNodeVariable()+1, generate((SimpleNode)n.jjtGetChild(0)));
				}
				existsString = "q\ne %s\nc\n\n\n%s/c\n/q\n";				
				return String.format(existsString, n.getNodeVariable()+1, generate((SimpleNode)n.jjtGetChild(0)));
			case AND:
				if(n.jjtGetChild(0).getNodeType() == SimpleNode.NodeType.AND) {
					left = String.format("d\n\n\n%s/d\n", generate((SimpleNode)n.jjtGetChild(0)));
				} else {
					left = generate((SimpleNode)n.jjtGetChild(0));
				}
				
				if(n.jjtGetChild(1).getNodeType() == SimpleNode.NodeType.AND) {
					right = String.format("d\n\n\n%s/d\n", generate((SimpleNode)n.jjtGetChild(1)));
				} else {
					right = generate((SimpleNode)n.jjtGetChild(1));
				}
				
				whole = String.format("c\n\n\n%s%s/c\n", left, right);
				
				return whole;
			case OR:
				if(n.jjtGetChild(0).getNodeType() == SimpleNode.NodeType.OR) {
					left = String.format("c\n\n\n%s/c\n", generate((SimpleNode)n.jjtGetChild(0)));
				} else {
					left = generate((SimpleNode)n.jjtGetChild(0));
				}
				
				if(n.jjtGetChild(1).getNodeType() == SimpleNode.NodeType.OR) {
					right = String.format("c\n\n\n%s/c\n", generate((SimpleNode)n.jjtGetChild(1)));
				} else {
					right = generate((SimpleNode)n.jjtGetChild(1));
				}
				
				whole = String.format("d\n\n\n%s%s/d\n", left, right);
				
				return whole;
			case NOT:				
				if(n.jjtGetParent().getNodeType() == SimpleNode.NodeType.AND || ((SimpleNode)n.jjtGetParent()).isQuantifierNode())
					return String.format("d\n\n%d\n/d\n", n.jjtGetChild(0).getNodeVariable()+1);
				return String.format("c\n\n%d\n/c\n", n.jjtGetChild(0).getNodeVariable()+1);
			case VAR:
				if(n.jjtGetParent().getNodeType() == SimpleNode.NodeType.AND || ((SimpleNode)n.jjtGetParent()).isQuantifierNode())
					return String.format("d\n%d\n\n/d\n", n.getNodeVariable()+1);
				return String.format("c\n%d\n\n/c\n", n.getNodeVariable()+1);
			default:
				throw new IllegalStateException();
		}
	}
	
	/**
	 * Give parent node, receive a format string to insert string-representation of current node into
	 * @param n The parent node of the current node
	 * @return pseudo-connective container
	 */
	private String getPseudoConnectiveFormatString(SimpleNode n) {
		if(n.getNodeType() == SimpleNode.NodeType.AND) {
			return "d\n\n\n%s/d\n";
		} else if(n.getNodeType() == SimpleNode.NodeType.OR) {
			return "c\n\n\n%s/c\n";
		} else 
			return "c\n\n\n%s/c\n";
	}
	
}
