package main.java.master;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import main.java.common.rmi.SlaveRemote;

import org.apache.log4j.Logger;

public class SlaveRegistry implements Observer{

	static Logger logger = Logger.getLogger(SlaveRegistry.class);

	private HashMap<String, SlaveRemote> slaves = new HashMap<String, SlaveRemote>();
	private ConcurrentHashMap<SlaveRemote, Integer> runningComputations = new ConcurrentHashMap<SlaveRemote, Integer>();

	private volatile static SlaveRegistry instance;

	private SlaveRegistry() {
	}

	synchronized public static SlaveRegistry instance() {
		if (instance == null)
			instance = new SlaveRegistry();

		return instance;
	}

	synchronized public HashMap<String, SlaveRemote> getSlaves() {
		return slaves;
	}

	synchronized SlaveRemote acquireFreeSlave(TQbf tqbf) {	
		
		SlaveRemote slave = findFreeSlave();
		while(slave == null) {
			try {wait();} catch (InterruptedException e) {}
			slave = findFreeSlave();
		}
		
		tqbf.addObserver(this);
		this.runningComputations.put(slave, this.runningComputations.get(slave) + 1);
		logger.info("Slave acquired. Has #jobs: " + this.runningComputations.get(slave));
		return slave;
	}
	
	synchronized SlaveRemote findFreeSlave() {
		SlaveRemote slave = null;
		for(Entry<SlaveRemote, Integer> entry : runningComputations.entrySet()) {
			try {
				if(entry.getKey().getCores() > entry.getValue()) {
					slave = entry.getKey(); break;
				}
			} catch (RemoteException e) {
				logger.error("", e);
			}
		}
		return slave;
	}
	
	synchronized public void put(String hostName, SlaveRemote slave) {
		runningComputations.put(slave, 0);
		slaves.put(hostName, slave);
		notifyAll();
//		setChanged();
//		notifyObservers();
	}

//	synchronized public int getCoresWithSolver(String solver) {
//		int c = 0;
//		try {
//			for (SlaveRemote s : slaves.values()) {
//				if (s.getSolvers().contains(solver))
//					c += s.getCores();
//			}
//		} catch (RemoteException e) {
//			logger.error("", e);
//		}
//		return c;
//	}

//	synchronized public int freeCores() {
//		int f = 0;
//		for (SlaveRemote s : this.slaves.values()) {
//			try {
//				f += s.freeCores();
//			} catch (RemoteException e) {
//			}
//		}
//		return f;
//	}

//	synchronized public List<SlaveRemote> freeCoreSlaves() {
//		List<SlaveRemote> slaves = new ArrayList<SlaveRemote>();
//
//		for (SlaveRemote s : this.slaves.values()) {
////			try {
////				logger.info("slave: " + s.getHostName() + ", freecores: " + s.freeCores());
////			} catch (Exception e1) {
////			}
//			try {
//				if (s.freeCores() > 0)
//					slaves.add(s);
//			} catch (RemoteException e) {
//			}
//		}
//		return slaves;
//	}

	/**
	 * Observes tqbfs
	 */
	@Override
	synchronized public void update(Observable arg0, Object arg1) {
		TQbf tqbf = (TQbf) arg0;
		if(tqbf.isAborted() || tqbf.isError() || tqbf.isTerminated() || tqbf.isTimeout()) {
			Integer currentRunning = this.runningComputations.get(tqbf.getSlave());
			this.runningComputations.put(tqbf.getSlave(), currentRunning-1);
		}
			
		notifyAll();
	}

}
