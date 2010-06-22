package main.java.logic;

import java.util.Vector;

public class SimpleProbNetHeuristic implements Heuristic {

	@Override
	public Vector<Integer> decide(Qbf qbf) {
		double p = qbf.root.getTruthProbability();
		Vector<Integer> eV = (Vector<Integer>) qbf.eVars.clone();
		Vector<Integer> aV = (Vector<Integer>) qbf.aVars.clone();
		// If the formula has a high probability to become true => Exists first
		if(p > 0.5) {
			eV.addAll(qbf.aVars);
			return eV;
		}
		aV.addAll(qbf.eVars);
		return aV;
	}

}
