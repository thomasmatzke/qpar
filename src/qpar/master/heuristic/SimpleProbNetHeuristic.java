package qpar.master.heuristic;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import qpar.master.Qbf;


public class SimpleProbNetHeuristic extends Heuristic {

	
	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		Set<Integer> universals 		= new HashSet<Integer>();
		Set<Integer> existentials 		= new HashSet<Integer>();
		LinkedHashSet<Integer> sorted	= new LinkedHashSet<Integer>();
		
		for(Integer i : group) {
			if(qbf.isUniversalQuantified(i))
				universals.add(i); 
			else
				existentials.add(i);
		}
		
		if(qbf.root.getTruthProbability() > 0.5) {
			sorted.addAll(existentials);
			sorted.addAll(universals);
		} else {
			sorted.addAll(universals);
			sorted.addAll(existentials);
		}
				
		return sorted;
	}

	@Override
	public String getId() {
		return "probnet";
	}

}
