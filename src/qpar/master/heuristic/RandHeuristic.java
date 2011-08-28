package qpar.master.heuristic;

import java.util.LinkedHashSet;
import java.util.Set;

import qpar.common.Permuter;
import qpar.master.Qbf;


public class RandHeuristic extends Heuristic {

	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		Permuter p = new Permuter(group);
		return new LinkedHashSet<Integer>(p.next());
	}

	@Override
	public String getId() {
		return "rand";
	}

}
