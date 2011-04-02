package main.java.slave.solver;

import java.util.ArrayList;

import main.java.master.TQbf;

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
		}
		return solvers;
	}

	/**
	 * Returns a new solver-instance identified with the corresponding id
	 * 
	 * @param id
	 * @return
	 */
	public static Solver getSolver(String id, ResultHandler handler, TQbf tqbf) {
		if (id.equals("qpro")) {
			Solver q = new QProSolver(tqbf,handler);
			return q;
		}
		return null;
	}

}
