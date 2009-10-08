package main.java.slave;

import java.util.Hashtable;
import java.util.Vector;

import main.java.slave.solver.Solver;

import org.apache.log4j.Logger;

import sun.misc.Signal;

public class SignalHandler implements sun.misc.SignalHandler {

	static Logger logger = Logger.getLogger(SlaveDaemon.class);
	
	public void handle(Signal sig) {
		logger.info("Cought Signal " + sig.getName());
		logger.info("Killing workerthreads...");
		Hashtable<String, Solver> threads = SlaveDaemon.getThreads();
		for(Solver t : threads.values()) {
			t.kill();
		}
		logger.info("Informing MasterDaemon...");
		Vector<String> tqbf_ids = new Vector<String>();
		for(String t : SlaveDaemon.getThreads().keySet()) {
			tqbf_ids.add(t);
		}
		
		SlaveDaemon.master.sendShutdownMessage("Cought Signal " + sig.getName(), tqbf_ids);
		logger.info("Shutting down...");
		SlaveDaemon.master.disconnect();
	}

}
