package main.java.logic.heuristic;

import java.util.LinkedHashSet;
import java.util.Set;

import main.java.logic.Qbf;
import main.java.logic.parser.SimpleNode;

public class SimpleHeuristic extends Heuristic {

	LinkedHashSet<Integer> ret = new LinkedHashSet<Integer>();
	
	public SimpleHeuristic(Qbf qbf) {
		super(qbf);
	}

	public LinkedHashSet<Integer> getVariableOrder() {
		logger.info("Generating variable order...");
		long start = System.currentTimeMillis();
		traverse(qbf.root);
		long end = System.currentTimeMillis();
		logger.info("Variable order generated. Took " + (end-start)/1000 + " seconds.");
		return ret;
	}
	
	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group) {
		assert(false);
		return null;
	}

	public void traverse(SimpleNode n) {
		switch(n.nodeType) {
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
	
}
