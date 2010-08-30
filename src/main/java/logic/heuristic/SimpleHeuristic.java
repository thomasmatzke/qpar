package main.java.logic.heuristic;

import java.util.LinkedHashSet;
import java.util.Set;
import main.java.logic.Qbf;

public class SimpleHeuristic extends Heuristic {

	public SimpleHeuristic(Qbf qbf) {
		super(qbf);
	}

	public LinkedHashSet<Integer> getVariableOrder() {
		return new LinkedHashSet<Integer>(qbf.vars);
	}
	
	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group) {
		assert(false);
		return null;
	}

}
