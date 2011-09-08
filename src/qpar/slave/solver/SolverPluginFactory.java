package qpar.slave.solver;

import java.util.Set;

import org.apache.log4j.Logger;

import qpar.common.Configuration;

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
		return Configuration.getPlugins().keySet();
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
		String className = Configuration.getPlugins().get(solverName);
		
		Class clazz = Class.forName(className);
		
		SolverPlugin p = (SolverPlugin)clazz.newInstance();
		return p;
	}

	
}
