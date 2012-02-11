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

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.Configuration;
import qpar.common.ConfigurationFactory;
import qpar.common.rmi.MasterRemote;
import qpar.common.rmi.SlaveRemote;
import qpar.common.rmi.TQbfRemote;
import qpar.master.console.Shell;
import qpar.master.scheduling.ParallelJobsScheduler;
import qpar.master.scheduling.Scheduler;

/**
 * Handles communication with slaves
 * 
 * @author thomasm
 * 
 */
public class Master extends UnicastRemoteObject implements MasterRemote {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Master.class);

	transient private Shell shell;
	private Registry registry = null;

	public static ConcurrentHashMap<String, Boolean> resultCache = new ConcurrentHashMap<String, Boolean>();
	public static ExecutorService globalThreadPool = Executors.newCachedThreadPool();
	private int cacheHits = 0, cacheMisses = 0;
	transient private MulticastBeacon multicastBeacon;

	public static Configuration configuration = null;
	public static Scheduler scheduler = null;

	public Master(final String path) throws IOException {
		try {
			this.initialize();
			this.shell = new Shell(new BufferedReader(new FileReader(path)));
			this.shell.run();
		} finally {
			this.tearDown();
		}

	}

	public Master() throws NotBoundException, IOException {
		try {
			this.initialize();
			this.shell = new Shell();
			this.shell.run();
		} finally {
			this.tearDown();
		}

	}

	private void initialize() throws IOException {
		configuration = ConfigurationFactory.getConfiguration();
		scheduler = new ParallelJobsScheduler();

		if (!configuration.isValid()) {
			LOGGER.error("The configuration loaded from the configuration file is incomplete.");
			throw new IllegalArgumentException("The configuration loaded from the configuration file is incomplete.");
		}

		this.multicastBeacon = new MulticastBeacon();
		Master.globalThreadPool.execute(this.multicastBeacon);

		// Start the registry
		this.registry = LocateRegistry.createRegistry(1099);

		// Start own interface
		MasterRemote myInterface = this;
		this.registry.rebind("Master", myInterface);
		LOGGER.info(String.format("Master initialized. Bound names in the registry: %s", Arrays.asList(this.registry.list())));
	}

	final public void tearDown() {
		LOGGER.info("Tearing down Master...");
		Master.globalThreadPool.shutdownNow();
		if (this.multicastBeacon != null) {
			this.multicastBeacon.stop();
		}
		try {
			for (TQbf t : TQbf.tqbfs.values()) {
				UnicastRemoteObject.unexportObject(t, true);
			}
			UnicastRemoteObject.unexportObject(this, true);
			UnicastRemoteObject.unexportObject(this.registry, true);
			this.registry.unbind("Master");
			if (this.registry != null) {
				LOGGER.info(String.format("Master shut down. Bound names in the registry: %s", Arrays.asList(this.registry.list())));
			}
		} catch (NoSuchObjectException nsoe) {
			LOGGER.info("Master-object was not exported. Not unbinding.");
		} catch (Exception e) {
			LOGGER.error("", e);
		}

	}

	private static void usage() {
		LOGGER.error("Arguments: \"-i=INPUTFILE\"       specifies a batch-file");
		System.exit(-1);
	}

	@Override
	public void registerSlave(final SlaveRemote slave) throws RemoteException, UnknownHostException {
		LOGGER.info("Registering Slave. Hostname: " + slave.getHostName() + ", Cores: " + slave.getCores());

		SlaveRegistry.getInstance().put(slave.getHostName(), slave);
	}

	@Override
	public void ping() throws RemoteException {
	}

	@Override
	public void displaySlaveMessage(final String slave, final String message) throws RemoteException {
		LOGGER.info("Slave " + slave + " said: " + message);
	}

	public static void main(final String[] args) throws Throwable {
		Getopt go = new Getopt("Master", args, "i:");
		try {

			int c = go.getopt();
			if (c == -1) {
				new Master();
			} else if (c == 'i') {
				String arg = go.getOptarg();
				new Master(arg);
			} else {
				usage();
			}

		} catch (Throwable t) {
			LOGGER.error("", t);
			Mailer.sendExceptionMail(t);
			throw t;
		}
	}

	@Override
	public Boolean getCachedResult(final byte[] hash) throws RemoteException {
		Boolean result = Master.resultCache.get(new String(hash));
		if (result == null) {
			this.cacheMisses++;
		} else {
			this.cacheHits++;
		}
		LOGGER.info("ResultCache Hits: " + this.cacheHits + ", Misses: " + this.cacheMisses);
		return result;
	}

	@Override
	public void cacheResult(final byte[] hash, final boolean result) throws RemoteException {
		Master.resultCache.put(new String(hash), Boolean.valueOf(result));
	}

	@Override
	public TQbfRemote getWork() throws RemoteException, InterruptedException {
		return scheduler.pullWork();
	}

}
