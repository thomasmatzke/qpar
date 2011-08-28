package qpar.master.heuristic;

import java.util.LinkedHashSet;
import java.util.Set;

import qpar.master.Qbf;
import qpar.master.logic.parser.SimpleNode;


public class SimpleHeuristic extends Heuristic {

	LinkedHashSet<Integer> ret = new LinkedHashSet<Integer>();
		
	public LinkedHashSet<Integer> getVariableOrder(Qbf qbf) {
		
		traverse(qbf.root);
		
		return ret;
	}
	
	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		assert(false);
		return null;
	}

	public void traverse(SimpleNode n) {
		switch(n.getNodeType()) {
			case START:
				assert(n.jjtGetNumChildren() == 1);
				traverse((SimpleNode)n.jjtGetChild(0));
				break;
			case FORALL:
			case EXISTS:
				ret.add(n.var);
				assert(n.jjtGetNumChildren() == 1);
				traverse((SimpleNode)n.jjtGetChild(0));
				break;
			case NOT:
				assert(n.jjtGetNumChildren() == 1);
				traverse((SimpleNode)n.jjtGetChild(0));
				break;
			case AND:
			case OR:
				assert(n.jjtGetNumChildren() == 2);
				traverse((SimpleNode)n.jjtGetChild(0));
				traverse((SimpleNode)n.jjtGetChild(1));
				break;
			case VAR:
				break;
			default:
				assert(false);
		}
	}

	@Override
	public String getId() {
		return "simple";
	}
	
}
