package main.java.slave;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map.Entry;

import main.java.rmi.Result;
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
	Slave slaveDaemon = null;
	public SignalHandler(Slave slaveDaemon) {
		this.slaveDaemon = slaveDaemon;
	}

	public void handle(Signal sig) {
		logger.info("Cought Signal " + sig.getName());
		logger.info("Killing workerthreads...");
		for(Entry<String, Solver> entry : Slave.threads.entrySet()) {
			
			Result r = new Result();
			r.type = Result.Type.ERROR;
			r.tqbfId = entry.getValue().getTransmissionQbf().getId();
			r.jobId = entry.getValue().getTransmissionQbf().jobId;
			r.errorMessage = "Cought Signal " + sig.getName();
			entry.getValue().kill();
			try {
				Slave.master.returnResult(r);
			} catch (RemoteException e) {
				logger.error(e);
			}
		}
		
		try {
			Slave.master.unregisterSlave(slaveDaemon);
		} catch (RemoteException e) {
			logger.error(e);
		} catch (UnknownHostException e) {
			logger.error(e);
		}
		
		logger.info("Shutting down...");
		System.exit(0);
	}

}
