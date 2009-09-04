package main.java.logic;

import java.util.Vector;

public class HeuristicFactory {

	Vector<String> heuristics = null;
	
	HeuristicFactory() {
		heuristics = new Vector<String>();  
		//TODO heuristic vektor füllen, vielleicht auch von jeder heuristik
		// eine instanz erstellen und speichern, fürs erste einfach eine id
		// "test"
		heuristics.add("test");
	}
	
	public Vector<String> getAvailableHeuristics() {
		return heuristics;
	}
	
	public Heuristic getHeuristic(String id) {
		if (id == "test")
			return new SimpleHeuristic();
		return null;
	}
}
