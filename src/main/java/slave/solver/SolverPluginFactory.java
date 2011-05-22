package main.java.slave.solver;

import java.util.Set;

import main.java.QPar;

import org.apache.log4j.Logger;

/**
 * A solver-factory
 * 
 * @author thomasm
 * 
 */
public class SolverPluginFactory {
	static Logger logger = Logger.getLogger(SolverPluginFactory.class);
		
	/**
	 * Returns all available/implemented solver-plugins.
	 * 
	 * @return
	 */
	public Set<String> getavailableSolvers() {
		return QPar.getPlugins().keySet();
	}

	/**
	 * Returns a new solver-instance identified with the corresponding id
	 * 
	 * @param id
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static SolverPlugin getSolver(String solverName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String className = QPar.getPlugins().get(solverName);
		
		Class clazz = Class.forName(className);
		
		SolverPlugin p = (SolverPlugin)clazz.newInstance();
		return p;
	}

	
}
