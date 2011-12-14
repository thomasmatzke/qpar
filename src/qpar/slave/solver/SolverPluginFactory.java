/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
