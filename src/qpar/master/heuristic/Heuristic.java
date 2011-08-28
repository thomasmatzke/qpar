package qpar.master.heuristic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


import org.apache.log4j.Logger;

import qpar.master.Qbf;

public abstract class Heuristic {

	protected static Logger logger = Logger.getLogger(Heuristic.class);
		
	public LinkedHashSet<Integer> getVariableOrder(Qbf qbf) {
		LinkedHashSet<Integer> ordered = new LinkedHashSet<Integer>();
		for(Set<Integer> group : getDecisionGroups(qbf)) {
			Set<Integer> orderedGroup = sortGroup(group, qbf);
			if(!((Set<Integer>) orderedGroup).equals(group)) {
				logger.error("Incorrect heuristic. \nInput: " + group + "\nOutput: " + orderedGroup);
				throw new RuntimeException();
			}
			for(Integer variable : orderedGroup) {
				ordered.add(variable);
			}
		}
		
		return ordered;
	}
	
	public abstract LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf);
	
	public abstract String getId();
	
	/**
	 * Builds an ordered vector of groups of variables which can be ordered by heuristics.
	 * Input is the quantifier-dependency-tree in the qbf
	 * 
	 * @return A vector consisting of groups to be sorted by an heuristic
	 */
	protected ArrayList<Set<Integer>> getDecisionGroups(Qbf qbf) {
		ArrayList<Set<Integer>> groups = new ArrayList<Set<Integer>>();
		CondensedDependencyNode condensedRoot = qbf.dependencyGraphRoot.condense();
		logger.debug("Condensed Tree: \n" + condensedRoot.dump());
		
		int maxDepth = 0;
		for(CondensedDependencyNode c : condensedRoot.allSubnodes()) {
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
