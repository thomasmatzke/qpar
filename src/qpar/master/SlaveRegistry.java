/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package qpar.master;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import qpar.common.rmi.SlaveRemote;

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

//	synchronized SlaveRemote acquireFreeSlave(TQbf tqbf) {	
//		
//		SlaveRemote slave = findFreeSlave();
//		while(slave == null) {
//			try {wait();} catch (InterruptedException e) {}
//			slave = findFreeSlave();
//		}
//		
//		tqbf.addObserver(this);
//		this.runningComputations.put(slave, this.runningComputations.get(slave) + 1);
//		logger.info("Slave acquired. Has #jobs: " + this.runningComputations.get(slave));
//		return slave;
//	}
	
//	synchronized SlaveRemote findFreeSlave() {
//		SlaveRemote freeSlave = null;
//		for(SlaveRemote slave : slaves.values()) {
//			try {
//				if(slave.getCores() > getRunningComputationsOfSlave(slave)) {
//					freeSlave = slave; break;
//				}
//			} catch (RemoteException e) {
//				logger.error("", e);
//			}
//		}
//		return freeSlave;
//	}
	
//	synchronized public int getRunningComputationsOfSlave(SlaveRemote slave) {
//		return this.runningComputations.get(slave);
////		int numComps = 0;
////		try {
////			numComps = slave.getRunningComputations();
////		} catch (RemoteException e) {
////			logger.error("", e);
////		}
////		logger.info(numComps + " computations running on slave");
////		return numComps;
//	}
	
	synchronized public void put(String hostName, SlaveRemote slave) {
		runningComputations.put(slave, 0);
		slaves.put(hostName, slave);
		notifyAll();
//		setChanged();
//		notifyObservers();
	}

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
