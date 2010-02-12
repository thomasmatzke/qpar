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
		}
		return heuristics;
	}
	
	public static Heuristic getHeuristic(String id) {
		if (id == "simple")
			return new SimpleHeuristic();
		if (id == "rand")
			return new RandHeuristic();
		if (id == "litcount")
			return new LCHeuristic();
		return null;
	}
}
