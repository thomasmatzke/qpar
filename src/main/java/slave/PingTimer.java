package main.java.slave;

import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class PingTimer extends TimerTask {

	static Logger logger = Logger.getLogger(PingTimer.class);
	
	int interval;
	
	Slave slave = null;
	
	public PingTimer(int interval, Slave slave) {
		this. slave = slave;
		this.interval = interval;
		Timer t = new Timer();
		t.schedule(this, 0, interval * 1000);
	}
		
	@Override
	public void run() {
		// Make a call to the master. look for exceptions to see if
		// the master died. then try to reconnect
		
		try {
			Slave.getMaster().ping();
		} catch (RemoteException e) {
			if(slave.connected){
				logger.error("Ping to master failed");
				slave.connect();
			}
		}
		
	}

}
