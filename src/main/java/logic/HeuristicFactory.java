package main.java.logic;

import java.util.Vector;

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
	
	public static Heuristic getHeuristic(String id) {
		if (id.equals("simple"))
			return new SimpleHeuristic();
		if (id.equals("rand"))
			return new RandHeuristic();
		if (id.equals("litcount"))
			return new LCHeuristic();
		if (id.equals("probnet"))
			return new SimpleProbNetHeuristic();
		return null;
	}
}
