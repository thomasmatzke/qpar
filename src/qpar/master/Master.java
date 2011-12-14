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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;

import qpar.common.Configuration;
import qpar.common.rmi.MasterRemote;
import qpar.common.rmi.SlaveRemote;
import qpar.common.rmi.TQbfRemote;
import qpar.master.console.Shell;


/**
 * Handles communication with slaves
 * 
 * @author thomasm
 * 
 */
public class Master extends UnicastRemoteObject implements MasterRemote, Serializable {

	private static final long serialVersionUID = -6189223346936131655L;

	static Logger logger = Logger.getLogger(Master.class);
	
	transient private Shell shell;
	private Registry registry = null;
		
	public static ConcurrentHashMap<String, Boolean> resultCache = new ConcurrentHashMap<String, Boolean>();
	public static ExecutorService globalThreadPool = Executors.newCachedThreadPool();
	private int cacheHits = 0, cacheMisses = 0;
	private MulticastBeacon multicastBeacon;
	
	public Master(String path) throws UnknownHostException, SocketException, RemoteException, FileNotFoundException {
		initialize();
		shell = new Shell(new BufferedReader(new FileReader(path)));
		shell.run();
		tearDown();
	}
	
	public Master() throws RemoteException, NotBoundException, UnknownHostException, SocketException {		
		initialize();
		shell = new Shell();
		shell.run();
		tearDown();
	}
	
	private void initialize() throws UnknownHostException, SocketException, RemoteException {
		Configuration.loadConfig();
		
		multicastBeacon = new MulticastBeacon();
		Master.globalThreadPool.execute(multicastBeacon);
		
		// Start the registry
		registry = LocateRegistry.createRegistry(1099);
		
		// Start own interface
		MasterRemote myInterface = this;
		registry.rebind("Master", myInterface);
		logger.info(String.format("Master initialized. Bound names in the registry: %s", Arrays.asList(registry.list())));
	}
	

	public void tearDown() {
		logger.info("Tearing down Master...");
		Master.globalThreadPool.shutdownNow();
		multicastBeacon.stop();		
		try {
			for(TQbf t : TQbf.tqbfs.values()) {
				UnicastRemoteObject.unexportObject(t, true);
			}
			UnicastRemoteObject.unexportObject(this, true);
			UnicastRemoteObject.unexportObject(registry,true);
			registry.unbind("Master");
		} catch (Exception e) {
			logger.error("", e);
		} 
		try {
			logger.info(String.format("Master shut down. Bound names in the registry: %s", Arrays.asList(registry.list())));
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	private static void usage() {
		System.out
				.println("Arguments: \"-i=INPUTFILE\"       specifies a batch-file");
		System.exit(-1);
	}

	@Override
	public void registerSlave(SlaveRemote slave) throws RemoteException, UnknownHostException {
		logger.info("Registering Slave. Hostname: " + slave.getHostName() + ", Cores: " + slave.getCores() + ", Solvers: " + slave.getSolvers());
		
		SlaveRegistry.instance().put(slave.getHostName(), slave);
	}

	@Override
	public void ping() throws RemoteException {}

	@Override
	public void displaySlaveMessage(String slave, String message) throws RemoteException {
		logger.info("Slave " + slave + " said: " + message);
	}
	
	public static void main(String[] args) throws Throwable {
		GetOpt go = new GetOpt(args, "i:");
		
		try {
		
			int c = go.getNextOption();
			if(c == -1) {
				new Master();
			} else if(c == 'i') {
				String arg = go.getOptionArg();
				new Master(arg);
			} else {
				usage();
			}

		} catch (Throwable t) {
			logger.fatal("", t);
			Configuration.sendExceptionMail(t);
			throw t;
		}
	}

	@Override
	public Boolean getCachedResult(byte[] hash) throws RemoteException {
		Boolean result = Master.resultCache.get(new String(hash));
		if(result == null)
			cacheMisses++;
		else
			cacheHits++;
		logger.info("ResultCache Hits: " + cacheHits + ", Misses: " + cacheMisses);
		return result;
	}

	@Override
	public void cacheResult(byte[] hash, boolean result) throws RemoteException {
		Master.resultCache.put(new String(hash), Boolean.valueOf(result));
	}

	@Override
	public TQbfRemote getWork() throws RemoteException, InterruptedException {
		return Distributor.instance().getWork();
	}
	
}
