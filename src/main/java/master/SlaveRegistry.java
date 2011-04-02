package main.java.master;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import main.java.rmi.SlaveRemote;

import org.apache.log4j.Logger;

public class SlaveRegistry extends Observable implements Observer {

	static Logger logger = Logger.getLogger(SlaveRegistry.class);

	private HashMap<String, SlaveRemote> slaves = new HashMap<String, SlaveRemote>();

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

//	synchronized public void removeSlave(String hostname) {
//		SlaveRemote slave = slaves.remove(hostname);
//		try {
//			for (String jobId : slave.getCurrentJobs()) {
//				Job j = Job.getJobs().get(jobId);
//				j.abort("Slave unregistering.");
//			}
//		} catch (RemoteException e) {
//			logger.error("", e);
//		}
//		setChanged();
//		notifyObservers();
//	}

	synchronized public void put(String hostName, SlaveRemote slave) {
		slaves.put(hostName, slave);
		setChanged();
		notifyObservers();
	}

	synchronized public int getCoresWithSolver(String solver) {
		int c = 0;
		try {
			for (SlaveRemote s : slaves.values()) {
				if (s.getSolvers().contains(solver))
					c += s.getCores();
			}
		} catch (RemoteException e) {
			logger.error("", e);
		}
		return c;
	}

	public void waitForCoresWithSolver(int cores, String solver) {
		while (cores < SlaveRegistry.instance().getCoresWithSolver(solver)) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	synchronized public ArrayList<SlaveRemote> getSlavesWithSolver(String solver) throws RemoteException {
		ArrayList<SlaveRemote> slavesOut = new ArrayList<SlaveRemote>();
		for (SlaveRemote s : slaves.values()) {
			if (s.getSolvers().contains(solver))
				slavesOut.add(s);
		}
		return slavesOut;
	}

	synchronized public Set<String> getAllAvaliableSolverIds() throws RemoteException {
		Set<String> solverIds = new HashSet<String>();
		for (SlaveRemote s : slaves.values()) {
			solverIds.addAll(s.getSolvers());
		}
		return solverIds;
	}

	synchronized public int freeCores() {
		int f = 0;
		for (SlaveRemote s : this.slaves.values()) {
			try {
				f += s.freeCores();
			} catch (RemoteException e) {
			}
		}
		return f;
	}

	synchronized public List<SlaveRemote> freeCoreSlaves() {
		List<SlaveRemote> slaves = new ArrayList<SlaveRemote>();

		for (SlaveRemote s : this.slaves.values()) {
			try {
				logger.info("slave: " + s.getHostName() + ", freecores: " + s.freeCores());
			} catch (Exception e1) {
			}
			try {
				if (s.freeCores() > 0)
					slaves.add(s);
			} catch (RemoteException e) {
			}
		}
		return slaves;
	}

	@Override
	synchronized public void update(Observable arg0, Object arg1) {
		notifyAll();
	}

}
