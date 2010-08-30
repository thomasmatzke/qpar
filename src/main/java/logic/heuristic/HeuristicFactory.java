package main.java.logic.heuristic;

import java.util.Vector;

import main.java.logic.Qbf;


public class HeuristicFactory {

	private static Vector<String> heuristics;
	
	public static Vector<String> getAvailableHeuristics() {
		if(heuristics == null) {
			heuristics = new Vector<String>();
			heuristics.add("simple");
			heuristics.add("rand");
			heuristics.add("litcount");
			heuristics.add("probnet");
		}
		return heuristics;
	}
	
	public static Heuristic getHeuristic(String id, Qbf qbf) {
		if (id.equals("simple"))
			return new SimpleHeuristic(qbf);
		if (id.equals("rand"))
			return new RandHeuristic(qbf);
		if (id.equals("litcount"))
			return new LCHeuristic(qbf);
		if (id.equals("probnet"))
			return new SimpleProbNetHeuristic(qbf);
		return null;
	}
}
