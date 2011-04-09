//package main.java.master;
//
//import java.io.IOException;
//import java.net.UnknownHostException;
//import java.rmi.RemoteException;
//
//import main.java.common.rmi.SlaveRemote;
//
//import org.apache.log4j.Logger;
//
//public class TransportThread implements Runnable {
//	
//	static Logger logger = Logger.getLogger(TransportThread.class);
//	
//	TQbf sub = null;
//	SlaveRemote s = null;
//
//	public TransportThread(SlaveRemote s, TQbf sub, String solver)
//			throws UnknownHostException, RemoteException, IOException {
//		this.sub = sub;
//		this.s = s;
//	}
//
//	@Override
//	public void run() {
//		try {
//			Job.logger.info("Sending formula " + sub.getId() + " ...");
//			long start = System.currentTimeMillis();
//			s.computeTqbf(sub);
//			long stop = System.currentTimeMillis();
//			double time = (stop - start) / 1000.00;
//			Job.logger.info("Formula " + sub.getId() + " sent to host " + s.getHostName() + "... (" + time + " seconds");
//		} catch (IOException e) {
//			Job.logger.error("While sending formula " + sub.getId(), e);
//		}
//	}
//}