package qpar.master.heuristic;

import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import qpar.common.Permuter;
import qpar.master.DTNode;
import qpar.master.Qbf;
import qpar.master.logic.parser.Node;


public class ShallowHeuristic extends Heuristic {

	static Logger logger = Logger.getLogger(ShallowHeuristic.class);
	
	public ShallowHeuristic(Qbf qbf) {
		super(qbf);
	}

	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group) {
		LinkedHashSet<Integer> order = new LinkedHashSet<Integer>();
		Deque<Node> nodes = new LinkedList<Node>();
		
		nodes.add(qbf.root);
		
		while(order.size() < group.size()) {
			Node currentNode = nodes.poll();
			
			switch(currentNode.getNodeType()) {
				case FORALL:
				case EXISTS:
					nodes.addFirst(currentNode.jjtGetChild(0));
					break;
				case VAR:
					if(group.contains(Integer.valueOf(currentNode.getVar()))){
						order.add(currentNode.getVar());
					}							
					break;
				default:
					for(int i = 0; i < currentNode.jjtGetNumChildren(); i++) {
						nodes.add(currentNode.jjtGetChild(i));
					}
			}
			
		}	
		
//		Permuter p = new Permuter(group);
//		return new LinkedHashSet<Integer>(p.next());
		
		return order;
	}

}
