package main.java.logic.heuristic;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import main.java.logic.Qbf;
import main.java.master.Db;
import main.java.master.MasterDaemon;

public abstract class Heuristic {

	protected Qbf qbf;
	static Logger logger = Logger.getLogger(Qbf.class);
	
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
		
		assert(ordered.size() == DependencyNode.registry.size() - 1);
		return ordered;
	}
	
	public abstract LinkedHashSet<Integer> sortGroup(Set<Integer> group);
	
	
	protected Vector<Set<Integer>> getDecisionGroups() {
		assert(DependencyNode.registry.size()-1 == qbf.vars.size());
		
		Vector<Set<Integer>> groups = new Vector<Set<Integer>>();
		for(DependencyNode n : DependencyNode.registry.values()) {
			if(n.type == DependencyNode.NodeType.ROOT)
				continue;
			
			if(groups.size() < n.depth)
				groups.setSize(n.depth);
			
			if(groups.get(n.depth-1) == null) {
				groups.set(n.depth-1, new HashSet<Integer>());	
			}
			groups.get(n.depth-1).add(n.variable);
			
		}
		
		
		int ctr = 0;
		for(Set<Integer> set : groups) {
			ctr += set.size();
		}
		
		assert(ctr == DependencyNode.registry.size()-1);
		
		return groups;
	}	
	
//	protected Set<Integer> nextDecisionGroup() {
//		
//		// The "to" values of the map is the next group
//		List<Map<String, Object>> deps = Db.query("SELECT * FROM dependencies WHERE qbf_id = '" + qbf.id + "' AND from = '0'");
//		
//		// Dont need the edges anymore
//		Db.update("DELETE FROM dependencies WHERE qbf_id = '" + qbf.id + "' AND from = '0'");
//		
//		// Unique set of Nodes (next decision group)
//		Set<Integer> nextGroup = new HashSet<Integer>();
//		
//		// the group after the next (2nd next)
//		Set<Integer> secondNextGroup = new HashSet<Integer>();
//		
//		for(Map<String, Object> dep : deps) {
//			Integer to 		= (Integer)dep.get("to");
//			
//			if(nextGroup.add(to) == false) {
//				logger.error("Bug in the nextDecisionGroup() method. Or design fail :P");
//				MasterDaemon.bailOut();
//			}				
//			
//			// Get nodes for the 2nd-next decisiongroup
//			List<Map<String, Object>> nextEdges = Db.query("SELECT * FROM dependencies WHERE qbf_id = '" + qbf.id + "' AND from = '" + to + "'");
//			
//			// Dont need those edges
//			Db.update("DELETE FROM dependencies WHERE qbf_id = '" + qbf.id + "' AND from = '" + to + "'");
//			
//			for(Map<String, Object> edge : nextEdges) {
//				secondNextGroup.add((Integer)edge.get("to"));
//			}
//		}
//		
//		// Connect the nodes of the (2nd) next decisiongroup to root
//		for(Integer node : secondNextGroup) {
//			Db.update("INSERT INTO dependencies (qbf_id, from, to) VALUES ('" + qbf.id + "','0','" + node + "')");
//		}
//		
//		
//		return nextGroup;
//	}
//	
//	protected boolean hasNextDecisionGroup() {
//		if(Db.query("SELECT * FROM dependencies WHERE qbf_id = '" + qbf.id + "' AND from = '0'").size() > 0)
//			return true;
//		return false;
//	}

}
