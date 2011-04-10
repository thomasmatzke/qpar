package main.java.master;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import main.java.common.rmi.RemoteObserver;
import main.java.common.rmi.SlaveRemote;
import main.java.master.TQbf.State;

import org.apache.log4j.Logger;

public class Distributor implements Runnable, RemoteObserver, Serializable, Observer  {

	private static final long serialVersionUID = -6606810196441096609L;

	static Logger logger = Logger.getLogger(Distributor.class);
	
	private volatile static Distributor instance;
	
	private BlockingQueue<TQbf> queue = new LinkedBlockingQueue<TQbf>();
	
	private boolean run = true;
	
	private Distributor() throws RemoteException {} 
	
	synchronized public static Distributor instance() {
		if(instance == null) {
			try {
				instance = new Distributor();
				UnicastRemoteObject.exportObject(instance, 0);
			} catch (RemoteException e) {
				logger.error("", e);
			}
			
			Master.globalThreadPool.execute(instance);
		}
		
		return instance;
	}
	
	public void scheduleJob(Job j) {
		for(TQbf tqbf : j.subformulas) {		
			try { queue.put(tqbf); } catch (InterruptedException e) {logger.error("", e);}
			logger.info("Added tqbf " + tqbf.getId() + " to distribution queue.");
		}
	}
	
	@Override
	public void run() {
		while(run) {
			TQbf tqbf;
			try {
				tqbf = queue.take();
			} catch (InterruptedException e) {continue;}
			
			if(tqbf.isDontstart())
				continue;
			
//			List<SlaveRemote> slaves = SlaveRegistry.instance().freeCoreSlaves();
//			
//			synchronized(this) {
//				while(slaves.size() < 1){
//					try { wait(); } catch (InterruptedException e) {}
//					slaves = SlaveRegistry.instance().freeCoreSlaves();
////					logger.info("Distributor found " + slaves.size() + " free slaves.");
//				}
//			}
//			logger.info("freecoreslaves: " + slaves.size());
//			SlaveRemote s = slaves.get(0);
			
			logger.info("acquiring slave");
			SlaveRemote s = SlaveRegistry.instance().acquireFreeSlave(tqbf);
			logger.info("slave acquired");
			
			try {
				tqbf.addObserver((RemoteObserver)this);
			} catch (RemoteException e1) {logger.error("", e1);}
			tqbf.compute(s);
//			sendTqbf(tqbf, s);
			
			// We have to wait til the formula is started to get accurate measurement
			// of free cores
			synchronized(this) {
				while(tqbf.isNew()) {
					try { wait(); } catch (InterruptedException e) {}
				}
			}
			assert(!tqbf.isNew());
		}
	}
	
//	private void sendTqbf(TQbf tqbf, SlaveRemote slave) {
//		try {
//			synchronized(tqbf) {
//				if(tqbf.getState() == State.NEW)
//					Master.globalThreadPool.execute(new TransportThread(slave, tqbf, tqbf.getSolverId()));
//			}
//		} catch (Exception e) {
//			logger.error("", e);
//			tqbf.abort();
//		}
//	}
		
	public void stop() {
		this.run = false;
	}

	@Override
	public void update(Object o, Object arg) throws RemoteException {
		synchronized(this) {
			notifyAll();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		synchronized(this) {
			notifyAll();
		}
	}


}