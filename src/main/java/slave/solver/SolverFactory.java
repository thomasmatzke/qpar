package main.java.slave.solver;

import java.rmi.RemoteException;
import java.util.ArrayList;

import main.java.common.rmi.TQbfRemote;

import org.apache.log4j.Logger;

/**
 * A solver-factory
 * 
 * @author thomasm
 * 
 */
public class SolverFactory {
	static Logger logger = Logger.getLogger(SolverFactory.class);
	
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
	public static Solver getSolver(TQbfRemote tqbf) {
		String solverId;
		try {
			solverId = tqbf.getSolverId();
		} catch (RemoteException e) {
			logger.error("", e);
			return null;
		}
		if (solverId.equals("qpro")) {
			Solver q = new QProSolver(tqbf);
			return q;
		}
		return null;
	}

}
