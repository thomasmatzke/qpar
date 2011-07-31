package qpar.master.heuristic;

import java.util.ArrayList;

import qpar.master.Qbf;



public class HeuristicFactory {

	private static ArrayList<String> heuristics;
	
	public static ArrayList<String> getAvailableHeuristics() {
		if(heuristics == null) {
			heuristics = new ArrayList<String>();
			heuristics.add("simple");
			heuristics.add("rand");
			heuristics.add("litcount");
			heuristics.add("probnet");
			heuristics.add("shallow");
			heuristics.add("edgecount");
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
		if (id.equals("shallow"))
			return new ShallowHeuristic(qbf);
		if (id.equals("edgecount"))
			return new EdgeCountHeuristic(qbf);
		return null;
	}
}
