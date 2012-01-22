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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.rmi.SlaveRemote;

final public class SlaveRegistry implements Observer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SlaveRegistry.class);

	private final HashMap<String, SlaveRemote> slaves = new HashMap<String, SlaveRemote>();
	private final ConcurrentHashMap<SlaveRemote, Integer> runningComputations = new ConcurrentHashMap<SlaveRemote, Integer>();

	private static SlaveRegistry instance;

	private SlaveRegistry() {
	}

	synchronized public static SlaveRegistry getInstance() {
		if (instance == null) {
			instance = new SlaveRegistry();
		}

		return instance;
	}

	synchronized public HashMap<String, SlaveRemote> getSlaves() {
		return this.slaves;
	}

	synchronized public void put(final String hostName, final SlaveRemote slave) {
		this.runningComputations.put(slave, 0);
		this.slaves.put(hostName, slave);
		this.notifyAll();
		// setChanged();
		// notifyObservers();
	}

	/**
	 * Observes tqbfs
	 */
	synchronized public void update(final Observable arg0, final Object arg1) {
		TQbf tqbf = (TQbf) arg0;
		if (tqbf.isAborted() || tqbf.isError() || tqbf.isTerminated() || tqbf.isTimeout()) {
			Integer currentRunning = this.runningComputations.get(tqbf.getSlave());
			this.runningComputations.put(tqbf.getSlave(), currentRunning - 1);
		}

		this.notifyAll();
	}

}
