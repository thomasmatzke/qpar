package qpar.master.heuristic;

import java.util.LinkedHashSet;
import java.util.Set;

import qpar.common.Permuter;
import qpar.master.Qbf;


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
