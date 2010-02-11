package main.java.logic;

import java.util.Random;
import java.util.Vector;

import main.java.Permuter;

public class RandHeuristic implements Heuristic {

	@Override
	public Vector<Integer> decide(Qbf qbf) {
		Permuter p = new Permuter(qbf.allVars);
		return p.next();
	}

}
