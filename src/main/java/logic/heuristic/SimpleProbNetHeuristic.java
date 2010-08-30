package main.java.logic.heuristic;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import main.java.logic.Qbf;
import main.java.logic.parser.SimpleNode;

public class SimpleProbNetHeuristic extends Heuristic {

	public SimpleProbNetHeuristic(Qbf qbf) {
		super(qbf);
	}

	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group) {
		Set<Integer> universals 		= new HashSet<Integer>();
		Set<Integer> existentials 		= new HashSet<Integer>();
		LinkedHashSet<Integer> sorted	= new LinkedHashSet<Integer>();
		
		for(Integer i : group) {
			DependencyNode n = DependencyNode.registry.get(i);
			if(n.type == DependencyNode.NodeType.EXISTENTIAL) {
				existentials.add(i);
			} else if(n.type == DependencyNode.NodeType.UNIVERSAL) {
				universals.add(i);
			} else {
				assert(false);
			}
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

}
