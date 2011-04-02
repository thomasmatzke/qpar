package main.java.tree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import main.java.logic.parser.Node;
import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.SimpleNode.NodeType;

public class QproRepresentation {

	SimpleNode root;
	
	public QproRepresentation(ReducedInterpretation r) {
		this.root = r.getInterpretation();
	}
	
	public String getQproRepresentation() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("QBF\n" + (Collections.max(getVariableSet(root))) + "\n");
				
		if(root.getNodeType().equals(NodeType.START)) {
			buf.append(root.children[0]);
		} else {
			buf.append(root);
		}
				
		buf.append("QBF\n");
		return buf.toString();
	}
	
	
	private HashSet<Integer> getVariableSet(SimpleNode n) {
		HashSet<Integer> vars = new HashSet<Integer>();
		for(Node child : n.children) {
			if(child.getNodeType().equals(NodeType.VAR)) {
				vars.add(child.getVar());
			} else {
				vars.addAll(getVariableSet((SimpleNode)child));
			}
		}		
		return vars;
	}
	
	public String traverse(SimpleNode n) {
		String traversedTree = "";
		Vector<Integer> posLiterals = new Vector<Integer>();
		Vector<Integer> negLiterals = new Vector<Integer>();
		SimpleNode tmpNode = null;
		
		if (n.nodeType == NodeType.EXISTS) {
			NodeType nt = n.jjtGetParent().getNodeType(); 	
			if (nt != NodeType.FORALL)
				traversedTree += "q\n";
			traversedTree += "e ";

			// add the first var
			traversedTree += n.var + " ";
			
			tmpNode = (SimpleNode)n.jjtGetChild(0);

			while (tmpNode.getNodeType() == NodeType.EXISTS) {
				traversedTree += tmpNode.getVar() + " ";
				tmpNode = (SimpleNode)tmpNode.jjtGetChild(0);
			}
			traversedTree += "\n";
			traversedTree += traverse(tmpNode);
			if ((nt != NodeType.EXISTS) && (nt != NodeType.FORALL))
				traversedTree += "/q\n";
			
		}	
			
		if (n.nodeType == NodeType.FORALL) {
			NodeType nt = n.jjtGetParent().getNodeType(); 	
			if (nt != NodeType.EXISTS)
				traversedTree += "q\n";
			traversedTree += "a ";

			// add the first var
			traversedTree += n.var + " ";
			
			tmpNode = (SimpleNode)n.jjtGetChild(0);

			while (tmpNode.getNodeType() == NodeType.FORALL) {
				traversedTree += tmpNode.getVar() + " ";
				tmpNode = (SimpleNode)tmpNode.jjtGetChild(0);
			}
			traversedTree += "\n";
			traversedTree += traverse(tmpNode);
			if ((nt != NodeType.EXISTS) && (nt != NodeType.FORALL))
				traversedTree += "/q\n";
		}	
			
		if (n.nodeType == NodeType.AND) {
			/*
			 *if ((jjtGetParent().getNodeType() == NodeType.FORALL) || (jjtGetParent().getNodeType() == NodeType.EXISTS))
			 *    traversedTree += "\n";
			 */
			traversedTree += "c\n";
			posLiterals = (n.getPositiveLiterals(NodeType.AND, posLiterals));
			negLiterals = (n.getNegativeLiterals(NodeType.AND, negLiterals));

			for (int var : posLiterals)
				traversedTree += var + " ";
			traversedTree += "\n";

			for (int var : negLiterals)
				traversedTree += var + " ";
			traversedTree += "\n";

			traversedTree += getEnclosedFormula((SimpleNode)n, NodeType.OR);

			traversedTree += "/c\n";
		}

		if (n.nodeType == NodeType.OR) {
			/*
			 *if ((jjtGetParent().getNodeType() == NodeType.FORALL) || (jjtGetParent().getNodeType() == NodeType.EXISTS))
			 *    traversedTree += "\n";
			 */
			traversedTree += "d\n";
			posLiterals = (n.getPositiveLiterals(NodeType.OR, posLiterals));
			negLiterals = (n.getNegativeLiterals(NodeType.OR, negLiterals));

			for (int var : posLiterals)
				traversedTree += var + " ";
			traversedTree += "\n";

			for (int var : negLiterals)
				traversedTree += var + " ";
			traversedTree += "\n";

			traversedTree += getEnclosedFormula((SimpleNode)n, NodeType.AND);

			traversedTree += "/d\n";
		}

		return traversedTree;
	}
	
	public String getEnclosedFormula(SimpleNode n, NodeType op) {
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < n.jjtGetNumChildren(); i++) {
			if (n.jjtGetChild(i).getNodeType() == op) {
				tmp.append(traverse((SimpleNode)n.jjtGetChild(i)));
			} else if ((n.jjtGetChild(i).getNodeType() == NodeType.EXISTS) || 
			           (n.jjtGetChild(i).getNodeType() == NodeType.FORALL)) {
				tmp.append(traverse((SimpleNode) n.jjtGetChild(i)));
			} else {
				tmp.append(getEnclosedFormula((SimpleNode)n.jjtGetChild(i), op));
			}
		}
		return tmp.toString();
	}
}
