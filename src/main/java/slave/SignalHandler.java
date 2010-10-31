package main.java.slave;

import java.util.Hashtable;
import java.util.Map.Entry;

import main.java.slave.solver.Solver;

import org.apache.log4j.Logger;

import sun.misc.Signal;

/**
 * Handles external signals to the Java VM to exit gracefully
 * @author thomasm
 *
 */
public class SignalHandler implements sun.misc.SignalHandler {

	static Logger logger = Logger.getLogger(SignalHandler.class);
		
	public void handle(Signal sig) {
		logger.info("Cought Signal " + sig.getName());
		logger.info("Killing workerthreads...");
		Hashtable<String, Solver> threads = SlaveDaemon.getThreads();
		for(Entry<String, Solver> entry : threads.entrySet()) {
			entry.getValue().kill();
			SlaveDaemon.master.sendFormulaAbortedMessage(entry.getKey());
		}
		
		if(SlaveDaemon.master.isConnected()) {
			logger.info("Informing MasterDaemon...");
			logger.info("Shutting down...");
			SlaveDaemon.master.disconnect();
		}
		System.exit(0);
	}

}
