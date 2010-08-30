package main.java.logic.heuristic;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import main.java.Permuter;
import main.java.logic.Qbf;

public class RandHeuristic extends Heuristic {

	public RandHeuristic(Qbf qbf) {
		super(qbf);
	}

	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group) {
		Permuter p = new Permuter(group);
		return new LinkedHashSet<Integer>(p.next());
	}

}
