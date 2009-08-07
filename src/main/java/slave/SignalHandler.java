package main.java.slave;

import sun.misc.Signal;

public class SignalHandler implements sun.misc.SignalHandler {

	@Override
	public void handle(Signal sig) {
		System.err.println("Cought Signal " + sig.getName());
		System.err.println("Informing Masterserver...");
		SlaveDaemon.master.sendSlaveShutdownMessage("Cought Signal " + sig.getName());
		System.err.println("Cleanup...");
		SlaveDaemon.shutdown();
	}

}
