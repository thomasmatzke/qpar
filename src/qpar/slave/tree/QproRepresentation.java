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
		
		buf.append("QBF\n" + (Collections.max(root.getVariableSet())) + "\n");
				
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
			
			if(!parent.isForallNode())
				traversedTree.append("q\n");
			traversedTree.append("e ");

			// add the first var
			traversedTree.append(n.var + " ");
			
			tmpNode = (SimpleNode)n.jjtGetChild(0);

			while (tmpNode.isExistsNode()) {
				traversedTree.append(tmpNode.getVar() + " ");
				tmpNode = (SimpleNode)tmpNode.jjtGetChild(0);
			}
			traversedTree.append("\n");
			traversedTree.append(traverse(tmpNode));
			if (!((SimpleNode)n.jjtGetParent()).isQuantifierNode())
				traversedTree.append("/q\n");
			
		}	
			
		if (n.isForallNode()) {	
			if(!parent.isExistsNode())
				traversedTree.append("q\n");
			traversedTree.append("a ");

			// add the first var
			traversedTree.append(n.var + " ");
			
			tmpNode = (SimpleNode)n.jjtGetChild(0);

			while (tmpNode.getNodeType() == NodeType.FORALL) {
				traversedTree.append(tmpNode.getVar() + " ");
				tmpNode = (SimpleNode)tmpNode.jjtGetChild(0);
			}
			traversedTree.append("\n");
			traversedTree.append(traverse(tmpNode));
			if(!parent.isQuantifierNode())
				traversedTree.append("/q\n");
		}	
			
		if (n.isAndNode()) {
			/*
			 *if ((jjtGetParent().getNodeType() == NodeType.FORALL) || (jjtGetParent().getNodeType() == NodeType.EXISTS))
			 *    traversedTree += "\n";
			 */
			traversedTree.append("c\n");
			posLiterals = (n.getPositiveLiterals(NodeType.AND, posLiterals));
			negLiterals = (n.getNegativeLiterals(NodeType.AND, negLiterals));

			for (int var : posLiterals)
				traversedTree.append(" " + var);
			traversedTree.append(" \n");

			for (int var : negLiterals)
				traversedTree.append(" " + var);
			traversedTree.append(" \n");

			traversedTree.append(getEnclosedFormula((SimpleNode)n, NodeType.OR));

			traversedTree.append("/c\n");
		}

		if (n.isOrNode()) {
			/*
			 *if ((jjtGetParent().getNodeType() == NodeType.FORALL) || (jjtGetParent().getNodeType() == NodeType.EXISTS))
			 *    traversedTree += "\n";
			 */
			traversedTree.append("d\n");
			posLiterals = (n.getPositiveLiterals(NodeType.OR, posLiterals));
			negLiterals = (n.getNegativeLiterals(NodeType.OR, negLiterals));

			for (int var : posLiterals)
				traversedTree.append(" " + var);
			traversedTree.append(" \n");

			for (int var : negLiterals)
				traversedTree.append(" " + var);
			traversedTree.append(" \n");

			traversedTree.append(getEnclosedFormula((SimpleNode)n, NodeType.AND));

			traversedTree.append("/d\n");
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
