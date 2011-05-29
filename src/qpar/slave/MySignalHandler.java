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
