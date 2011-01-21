package main.java.slave.solver;

import java.util.Vector;

import main.java.slave.Slave;

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
	public static Solver getSolver(String id, Slave slave) {
		if (id.equals("qpro")) {
			Solver q = new QProSolver(slave);
			Thread t = new Thread(q);
			q.setThread(t);
			return q;
		}
		if (id.equals("simple")) {
			Solver q = new SimpleSolver(slave);
			Thread t = new Thread(q);
			q.setThread(t);
			return q;
		}
		return null;
	}

}
