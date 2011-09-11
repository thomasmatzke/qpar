package qpar.slave.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	
	public void newReduce() {
		for(Node node : reducableNodes) {
			if(!node.checkConnectionToRoot()) 
				continue;
			
			SimpleNode current = (SimpleNode) node;
			SimpleNode newNode = null;
			
			do {
				logger.debug("Reducing node " + current +" with children " + Arrays.toString(current.children));
				if(current.jjtGetNumChildren() == 2){
					assert((SimpleNode)current.jjtGetChild(0) != null);
					assert((SimpleNode)current.jjtGetChild(1) != null);
					newNode = evaluate(current, (SimpleNode)current.jjtGetChild(0), (SimpleNode)current.jjtGetChild(1));
				}else if(current.jjtGetNumChildren() == 1){
					if(!(((SimpleNode)current).isNotNode() || ((SimpleNode)current).isQuantifierNode() || ((SimpleNode)current).isStartNode())){
						logger.error(String.format("Node of type: %s, but has 1 child", ((SimpleNode)current).getNodeType()));
						throw new IllegalStateException();
					}
					newNode = evaluate(current, (SimpleNode)current.jjtGetChild(0), null);
				} else
					throw new RuntimeException("Node must have 1 or 2 children. Has: " + current.jjtGetNumChildren());
				
				newNode.jjtSetParent(current.jjtGetParent());
				for(int i = 0; i < current.jjtGetParent().jjtGetNumChildren(); i++) {
					if(current.jjtGetParent().jjtGetChild(i) == current) {
						current.jjtGetParent().jjtAddChild(newNode, i);
					}
				}
				logger.debug("Reduced to: " + newNode);
				current = (SimpleNode) newNode.jjtGetParent();
			} while(newNode.isTruthNode() && !current.isStartNode());
			
		}
		
	}
	
	
	public void reduce() {
		newReduce();
		
//		while(!reducableNodes.isEmpty()) {
////			logger.info("Reducable nodes(" + reducableNodes.size() + "): " + reducableNodes);
//			SimpleNode current = reducableNodes.pollFirst();
//			
//			//if we are about to reduce the START node we are done
//			if(current.getNodeType() == NodeType.START)
//				return;
//			
//			SimpleNode newReducable = reduceNode(current);
//			
//			if(newReducable != null)
//				reducableNodes.addFirst(newReducable);
//		}
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
		logger.debug("Reducing node " + node +" with children " + Arrays.toString(node.children));
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
		
		
		logger.debug("Reduced to: " + newNode);
		
//		if(node.isQuantifierNode() && node.getNodeVariable() == 908){
//			node.jjtGetParent().dump("**");
//		}
		
		// Disconnect ourself from parent node
		node.jjtSetParent(null);
		
		if(newNode.isTruthNode())
			assert(newNode.children == null);
		
		
		// if the new node is a truth node, return its parent for further reducing
		if(newNode.isTruthNode())
			return (SimpleNode) newNode.jjtGetParent();
		
		if(newNode.isQuantifierNode() || newNode.isNotNode())
			assert(newNode.children.length == 1);
		if(newNode.isAndNode() || newNode.isOrNode())
			assert(newNode.children.length == 2);
		if(newNode.isVarNode())
			assert(newNode.children == null);
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
		
		// Panic if none of the children are truth-nodes
		if(!left.isTruthNode() && right != null && !right.isTruthNode())
			throw new RuntimeException();
		
		// Handle case where both children are truth nodes
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
				newNode.setTruthValue(left.getTruth());
				break;
			case NOT:
				assert(parent.isNotNode());
				assert(right == null);
				assert(left.isTruthNode());
//				logger.info("Left type before: " + left.getNodeType());
				newNode.setTruthValue( ! left.getTruth());
//				logger.info("Left type after: " + left.getNodeType());
				break;
			case AND:
				assert(parent.isAndNode());
				//logger.debug(String.format("parent: %s, left: %s, right: %s", parent.getNodeType(), left.getNodeType(), right.getNodeType()));
				if(left.isTruthNode())
					return evaluate(parent, right, left);
				if(right.getTruth())
					return left;
				newNode.setTruthValue(false);
				break;
			case OR:
				if(left.isTruthNode())
					return evaluate(parent, right, left);
				if(!right.getTruth())
					return left;
				newNode.setTruthValue(true);
				break;
			case VAR:
			case FALSE:
			case TRUE:
			default:
				throw new IllegalStateException();
		}
		
		assert(newNode != null);
		return newNode;
		
	}

}
