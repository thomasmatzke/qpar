package main.java.slave.solver;

import java.util.ArrayList;
import java.util.Vector;

import main.java.logic.TransmissionQbf;

/**
 * A solver-factory
 * 
 * @author thomasm
 * 
 */
public class SolverFactory {
	private static ArrayList<String> solvers;

	/**
	 * Returns all available/implemented solvers.
	 * 
	 * @return
	 */
	public static ArrayList<String> getavailableSolvers() {
		if (solvers == null) {
			solvers = new ArrayList<String>();
			solvers.add("qpro");
			solvers.add("simple");
		}
		return solvers;
	}

	/**
	 * Returns a new solver-instance identified with the corresponding id
	 * 
	 * @param id
	 * @return
	 */
	public static Solver getSolver(String id, ResultHandler handler, TransmissionQbf tqbf) {
		if (id.equals("qpro")) {
			Solver q = new QProSolver(tqbf,handler);
			Thread t = new Thread(q);
			q.setThread(t);
			return q;
		}
		if (id.equals("simple")) {
			Solver q = new SimpleSolver(tqbf, handler);
			Thread t = new Thread(q);
			q.setThread(t);
			return q;
		}
		return null;
	}

}
