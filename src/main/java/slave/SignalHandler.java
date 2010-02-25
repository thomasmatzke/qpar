package main.java.slave;

import java.util.Hashtable;
import java.util.Vector;

import main.java.slave.solver.Solver;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import sun.misc.Signal;

/**
 * Handles external signals to the Java VM to exit gracefully
 * @author thomasm
 *
 */
public class SignalHandler implements sun.misc.SignalHandler {

	static Logger logger = Logger.getLogger(SlaveDaemon.class);
	{
		logger.setLevel(Level.INFO);
	}
	
	public void handle(Signal sig) {
		logger.info("Cought Signal " + sig.getName());
		logger.info("Killing workerthreads...");
		Hashtable<String, Solver> threads = SlaveDaemon.getThreads();
		for(Solver t : threads.values()) {
			t.kill();
		}
		logger.info("Informing MasterDaemon...");
				
		SlaveDaemon.master.sendShutdownMessage("Cought Signal " + sig.getName());
		logger.info("Shutting down...");
		SlaveDaemon.master.disconnect();
	}

}
