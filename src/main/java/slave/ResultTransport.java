package main.java.slave;

import java.rmi.RemoteException;

import main.java.rmi.MasterRemote;
import main.java.rmi.Result;

import org.apache.log4j.Logger;

/**
 * Class to prevent circular waiting/deadlocking
 * @author thomasm
 *
 */
public class ResultTransport implements Runnable {

	private MasterRemote master;
	private Result r;
	static Logger logger = Logger.getLogger(FormulaReceiver.class);
	
	public  ResultTransport(MasterRemote master, Result r) {
		this.master = master;
		this.r = r;
	}
	
	@Override
	public void run() {
		try {
			master.returnResult(r);
		} catch (RemoteException e) {
			logger.error("RMI fail", e);
		}
	}

}
