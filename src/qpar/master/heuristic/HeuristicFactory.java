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
			//heuristics.add("htest");
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
		if (id.equals("shallow"))
			return new ShallowHeuristic();
		if (id.equals("edgecount"))
			return new EdgeCountHeuristic();
		if (id.equals("htest"))
			return new HTestHeuristic();
		return null;
	}
}
