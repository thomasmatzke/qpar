package main.java.slave;

import org.apache.log4j.Logger;

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
		synchronized(ComputationStateMachine.computations) {
			for(ComputationStateMachine machine : ComputationStateMachine.computations.values()) {
				machine.abortComputation();
			}
		}
		
//		synchronized(slaveDaemon.threads) {
//			for(Entry<String, Solver> entry : slaveDaemon.threads.entrySet()) {
//				
//				Result r = new Result();
//				r.type = Result.Type.ERROR;
//				r.tqbfId = entry.getValue().getTransmissionQbf().getId();
//				r.jobId = entry.getValue().getTransmissionQbf().jobId;
//				r.errorMessage = "Cought Signal " + sig.getName();
//				entry.getValue().kill();
//				try {
//					slaveDaemon.master.returnResult(r);
//				} catch (RemoteException e) {
//					logger.error(e);
//				}
//			}
//			
//			try {
//				if(slaveDaemon.master != null)
//					slaveDaemon.master.unregisterSlave(slaveDaemon);
//			} catch (RemoteException e) {
//				logger.error(e);
//			} catch (UnknownHostException e) {
//				logger.error(e);
//			}
//		}
		logger.info("Shutting down...");
		System.exit(0);
	}

}
