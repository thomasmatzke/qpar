package main.java.slave;

import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class PingTimer extends TimerTask {

	static Logger logger = Logger.getLogger(PingTimer.class);
	
	int interval;
	
	public PingTimer(int interval) {
		this.interval = interval;
		Timer t = new Timer();
		t.schedule(this, 0, interval * 1000);
	}
	
	@Override
	public void run() {
		// Make a call to the master. look for exceptions to see if
		// the master died. the try to reconnect
		
		try {
			Slave.master.ping();
		} catch (RemoteException e) {
			if(Slave.connected){
				logger.error("Master probably dead: " + e);
				logger.error("Killing threads...");
				Slave.instance.killAllThreads();
				Slave.connected = false;
				logger.error("Reconnecting...");
				try {
					Slave.instance.connect();
				} catch (InterruptedException e1) {}
			}
		}
		
	}

}
