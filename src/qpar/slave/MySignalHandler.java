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
package qpar.slave;


import org.apache.log4j.Logger;

import qpar.slave.solver.Solver;
import sun.misc.Signal;

/**
 * Handles external signals to the Java VM to exit gracefully
 * @author thomasm
 *
 */
public class MySignalHandler implements sun.misc.SignalHandler {

	static Logger logger = Logger.getLogger(MySignalHandler.class);
	Slave slaveDaemon = null;
	public MySignalHandler(Slave slaveDaemon) {
		this.slaveDaemon = slaveDaemon;
	}

	public void handle(Signal sig) {
		logger.info("Cought Signal " + sig.getName());
		logger.info("Killing workerthreads...");
		for(Solver solver : Solver.solvers.values()) {
			solver.kill();
		}

		logger.info("Shutting down...");
		System.exit(0);
	}

}
