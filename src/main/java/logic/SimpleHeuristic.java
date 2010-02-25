package main.java.logic;

import java.util.Vector;

public class SimpleHeuristic implements Heuristic {

	SimpleHeuristic() {}

	@Override
	public Vector<Integer> decide(Qbf qbf) {
		return qbf.vars;
	}

}
