package main.java.slave.solver;

import java.util.Vector;

/**
 * A solver-factory
 * 
 * @author thomasm
 * 
 */
public class SolverFactory {
	private static Vector<String> solvers;

	/**
	 * Returns all available/implemented solvers.
	 * 
	 * @return
	 */
	public static Vector<String> getavailableSolvers() {
		if (solvers == null) {
			solvers = new Vector<String>();
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
	public static Solver getSolver(String id) {
		if (id.equals("qpro")) {
			Solver q = new QProSolver();
			Thread t = new Thread(q);
			q.setThread(t);
			return q;
		}
		return null;
	}

}
