package qpar.slave.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;


import org.apache.log4j.Logger;

import qpar.master.logic.parser.Node;
import qpar.master.logic.parser.SimpleNode;
import qpar.master.logic.parser.SimpleNode.NodeType;

/**
 * Reduces a tree of simplenodes following boolean rules
 * @author thomasm
 *
 */
public class Reducer {
	static Logger logger = Logger.getLogger(Reducer.class);
	ArrayDeque<SimpleNode> reducableNodes;
	
	public Reducer(ArrayDeque<SimpleNode> reducableNodes) {
		this.reducableNodes = reducableNodes;
	}
	
	public void reduce() {
		while(!reducableNodes.isEmpty()) {
//			logger.info("Reducable nodes(" + reducableNodes.size() + "): " + reducableNodes);
			SimpleNode current = reducableNodes.pollFirst();
			
			//if we are about to reduce the START node we are done
			if(current.getNodeType() == NodeType.START)
				return;
			
			SimpleNode newReducable = reduceNode(current);
			
			if(newReducable != null)
				reducableNodes.addFirst(newReducable);
		}
	}
		
	
	/**
	 * Uses evaluate to find the replacement for this node
	 * makes and cuts the neccessary connections (parent, children, etc)
	 * @return further reducable node or else null
	 */
	public SimpleNode reduceNode(SimpleNode node) {
		// No need to reduce if we are disconnected from root
		// TODO: possible to eliminate this check?
		if(!node.checkConnectionToRoot()) {
//			logger.info("Disconnected from root");
			return null;
		}
//logger.info("Reducing node " + node +" with children " + Arrays.toString(node.children));
		SimpleNode newNode = null;
		if(node.jjtGetNumChildren() == 2)
			newNode = evaluate(node, (SimpleNode)node.jjtGetChild(0), (SimpleNode)node.jjtGetChild(1));
		else if(node.jjtGetNumChildren() == 1)
			newNode = evaluate(node, (SimpleNode)node.jjtGetChild(0), null);
		else
			throw new RuntimeException("Node must have 1 or 2 children. Has: " + node.jjtGetNumChildren());
		
		SimpleNode parentNode = (SimpleNode) node.jjtGetParent();
		
		// Connect parent with new node
		newNode.jjtSetParent(parentNode);
		ArrayList<Node> newParentChildren = new ArrayList<Node>();
		
		for(Node n : parentNode.children) {
			if(n != node)
				newParentChildren.add(n);
		}
		newParentChildren.add(newNode);
		parentNode.children = newParentChildren.toArray(new SimpleNode[newParentChildren.size()]);
//logger.info("Reduced to: " + newNode);
		
		// Disconnect ourself from parent node
		node.jjtSetParent(null);
		
		// if the new node is a truth node, return its parent for further reducing
		if(newNode.isTruthNode())
			return (SimpleNode) newNode.jjtGetParent();
		return null;
	}
	
	/** 
	 * Gets the new node resulting from boolean resolution
	 * left OR right MUST be TRUE or FALSE
	 * @param left
	 * @param right Set to null if there only is one child
	 * @return
	 */
	private SimpleNode evaluate(SimpleNode parent, SimpleNode left, SimpleNode right) {
		SimpleNode newNode = new SimpleNode();
		
		// Panic if none of the children are truth-node
		if(!left.isTruthNode() && right != null && !right.isTruthNode())
			throw new RuntimeException();
		
		// Handle case where both childs are truth nodes
		if(left.isTruthNode() && right != null && right.isTruthNode()) {
			switch(parent.getNodeType()) {
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
		switch(parent.getNodeType()) {
			case FORALL:
			case EXISTS:
				assert(right == null);
				assert(left.isTruthNode());
				return left;
			case NOT:
				assert(right == null);
				assert(left.isTruthNode());
//				logger.info("Left type before: " + left.getNodeType());
				left.setTruthValue( ! left.getTruth());
//				logger.info("Left type after: " + left.getNodeType());
				return left;				
			case AND:
				if(left.isTruthNode())
					return evaluate(parent, right, left);
				if(right.getTruth())
					return left;
				newNode.setTruthValue(false);
				return newNode;
			case OR:
				if(left.isTruthNode())
					return evaluate(parent, right, left);
				if(!right.getTruth())
					return left;
				newNode.setTruthValue(true);
				return newNode;
			case VAR:
			case FALSE:
			case TRUE:
			default:
				throw new IllegalStateException();
		}
		
	}

}
