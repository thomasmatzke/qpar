package main.java.slave;

import java.io.IOException;

import main.java.slave.solver.Solver;

import org.apache.activemq.transport.TransportListener;
import org.apache.log4j.Logger;

public class MsgBrokerTransportListener implements TransportListener {

	static Logger logger = Logger.getLogger(SlaveDaemon.class);
	
	@Override
	public void onCommand(Object arg0) {
		// not needed
	}

	@Override
	public void onException(IOException e) {
		logger.error(e);
		logger.error("Killing all running solver instances...");
		SlaveDaemon.master.connected = false;
		for(Solver thread : SlaveDaemon.threads.values()) {
			thread.kill();
		}
		SlaveDaemon.master.connect(SlaveDaemon.master_str);
	}

	@Override
	public void transportInterupted() {
		// not needed
	}

	@Override
	public void transportResumed() {
		// not needed
	}

}
