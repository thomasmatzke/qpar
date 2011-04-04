package main.java.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;

import main.java.logic.parser.Node;
import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.SimpleNode.NodeType;

import org.apache.log4j.Logger;

/**
 * Takes a SimpleNode and assigns truth values to variables in the subtree
 * @author thomasm
 *
 */
public class TruthAssigner {
	
	static Logger logger = Logger.getLogger(TruthAssigner.class);
	
	SimpleNode root;
	ArrayList<Integer> trueVars;
	ArrayList<Integer> falseVars;
	
	public TruthAssigner(SimpleNode root, ArrayList<Integer> trueVars, ArrayList<Integer> falseVars) {
		this.root = root;
		this.trueVars = trueVars;
		this.falseVars = falseVars;
	}
	
	/**
	 * Assigns nodes and returns them
	 * @return
	 */
	public ArrayDeque<SimpleNode> assign() {
//		logger.info("Assigning " + Arrays.toString(trueVars.toArray()) + ", " + Arrays.toString(falseVars.toArray()));
		ArrayDeque<SimpleNode> assigned = assignNode(root);
//		logger.info("Assigned " + assigned.size() + " nodes.");
		return assigned;
	}
		
	private ArrayDeque<SimpleNode> assignNode(SimpleNode node) {
		ArrayDeque<SimpleNode> ret = new ArrayDeque<SimpleNode>();
		if (node.nodeType.equals(NodeType.VAR)) {
			if(trueVars.contains(node.getVar())) {
				node.setNodeType(NodeType.TRUE);
				ret.add(node);
			} else if(falseVars.contains(node.getVar())) {
				node.setNodeType(NodeType.FALSE);
				ret.add(node);
			}
			
		} else {
			for(Node n : node.children) {
				ret.addAll(assignNode((SimpleNode) n));
			}
		}
		return ret;
	}
	
}
