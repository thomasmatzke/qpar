package main.java.logic;

import java.util.Vector;

public class HeuristicFactory {

	private static Vector<String> heuristics;
	
	public static Vector<String> getAvailableHeuristics() {
		if(heuristics == null) {
			heuristics = new Vector<String>();
			heuristics.add("test");
		}
		return heuristics;
	}
	
	public static Heuristic getHeuristic(String id) {
		if (id == "test")
			return new SimpleHeuristic();
		return null;
	}
}
