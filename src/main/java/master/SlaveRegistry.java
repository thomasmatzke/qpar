package main.java.master;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import main.java.rmi.SlaveRemote;

public class SlaveRegistry extends Observable {
	private HashMap<String, SlaveRemote> slaves = new HashMap<String, SlaveRemote>();
	
	private volatile static SlaveRegistry instance;
	
	private SlaveRegistry() {}
	
	public static SlaveRegistry instance() {
		if(instance == null)
			instance = new SlaveRegistry();
		
		return instance;
	}
	
	synchronized public HashMap<String, SlaveRemote> getSlaves() {
		return slaves;
	}
	
	synchronized public void removeSlave(String hostname) {
		slaves.remove(hostname);
		setChanged();
        notifyObservers();
	}

	synchronized public void put(String hostName, SlaveRemote slave) {
		slaves.put(hostName, slave);
		setChanged();
        notifyObservers();
	}

	synchronized public int getCoresWithSolver(String solver) throws RemoteException {
		int c = 0;
		for(SlaveRemote s : slaves.values()) {
			if(s.getSolvers().contains(solver))
				c += s.getCores();
		}
		return c;
	}

	synchronized public ArrayList<SlaveRemote> getSlavesWithSolver(String solver) throws RemoteException {
		ArrayList<SlaveRemote> slavesOut = new ArrayList<SlaveRemote>();
		for(SlaveRemote s : slaves.values()) {
			if(s.getSolvers().contains(solver))
				slavesOut.add(s);
		}
		return slavesOut;
	}

	synchronized public Set<String> getAllAvaliableSolverIds() throws RemoteException {
		Set<String> solverIds = new HashSet<String>();
		for(SlaveRemote s : slaves.values()) {
			solverIds.addAll(s.getSolvers());
		}
		return solverIds;
	}
	
	synchronized public int freeCores()  {
		int f = 0;
		for(SlaveRemote s : slaves.values()) {
			try { f += s.freeCores(); } catch (RemoteException e) {}
		}
		return f;
	}
	
}
