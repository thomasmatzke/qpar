package main.java.logic.heuristic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import main.java.logic.Qbf;

import org.apache.log4j.Logger;

public abstract class Heuristic {

	protected Qbf qbf;
	protected static Logger logger = Logger.getLogger(Heuristic.class);
	
	public Heuristic(Qbf qbf) {
		this.qbf = qbf;
	}
	
	public LinkedHashSet<Integer> getVariableOrder() {
		LinkedHashSet<Integer> ordered = new LinkedHashSet<Integer>();
		for(Set<Integer> group : getDecisionGroups()) {
			Set<Integer> orderedGroup = sortGroup(group);
			for(Integer variable : orderedGroup) {
				ordered.add(variable);
			}
		}
		
		return ordered;
	}
	
	public abstract LinkedHashSet<Integer> sortGroup(Set<Integer> group);
	
	
	/**
	 * Builds an ordered vector of groups of variables which can be ordered by heuristics.
	 * Input is the quantifier-dependency-tree in the qbf
	 * 
	 * @return A vector consisting of groups to be sorted by an heuristic
	 */
	protected ArrayList<Set<Integer>> getDecisionGroups() {
		ArrayList<Set<Integer>> groups = new ArrayList<Set<Integer>>();
		CondensedDependencyNode condensedRoot = qbf.dependencyGraphRoot.condense();
		logger.debug("Condensed Tree: \n" + condensedRoot.dump());
		
		int maxDepth = 0;
		for(CondensedDependencyNode c : condensedRoot.allSubnodes()) {
			logger.info(c.getDepth());
			if(c.getDepth() == 0)
				continue;
			
			if(c.getDepth() > maxDepth)
				maxDepth = c.getDepth();
			
			int arrIndex = c.getDepth()-1;
			
			while(arrIndex >= groups.size())
				groups.add(new HashSet<Integer>());
				
			groups.get(arrIndex).addAll(c.variables);
						
		}
		assert(maxDepth == groups.size());
		return groups;
	}	
	

}
