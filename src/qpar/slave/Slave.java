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
package qpar.slave;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;

import qpar.common.Configuration;
import qpar.common.rmi.MasterRemote;
import qpar.common.rmi.SlaveRemote;
import qpar.common.rmi.TQbfRemote;
import qpar.slave.solver.Solver;
import sun.misc.Signal;

/**
 * Represents the slave-process. Handles parameter-parsing, Signal-handling
 * 
 * @author thomasm
 * 
 */
public final class Slave extends UnicastRemoteObject implements SlaveRemote {
	private static final long serialVersionUID = -7927545942720427850L;
	
	public ArrayList<String> availableSolvers = new ArrayList<String>();
	public ExecutorService globalThreadPool = Executors.newCachedThreadPool();

	public static String hostname = null;
	static Logger logger = Logger.getLogger(Slave.class);

	private MasterRemote master = null;
	

	private boolean run = true;
	public boolean connected = false;

	private String masterRmiString;

	private PingTimer pingTimer;
	
	
	public Slave() throws UnknownHostException, IOException, InterruptedException {
		BeaconListener bl = new BeaconListener();
		new Slave(bl.getMasterAddress());
	}
	
	public Slave(String masterHost) throws RemoteException, UnknownHostException {
		logger.info("Starting Slave...");
		
		Configuration.loadConfig();

		this.masterRmiString = "rmi://" + masterHost + ":1099/Master";

		try {
			Slave.hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			logger.fatal("Cant get hostname", e1);
			throw e1;
		}

		MySignalHandler handler = new MySignalHandler(this);
		Signal.handle(new Signal("INT"), handler);
		Signal.handle(new Signal("TERM"), handler);
		// Signal.handle(new Signal("HUP"), handler);

		logger.info("Available Solvers are: " + availableSolvers);

		connect();

		pingTimer = new PingTimer(10, this);
		
		BoundedExecutor bex = new BoundedExecutor(Solver.solverThreadPool, Configuration.getAvailableProcessors());
		
		while (this.run) {
				TQbfRemote tqbf = null;
				try {
					tqbf = this.getMaster().getWork();
				} catch (InterruptedException e1) {}
				if(tqbf == null) {
					if(!this.run)
						break;
					else
						continue;
				}
			try {
				tqbf.setSlave(this);
				Solver solver = new Solver(tqbf, this.getMaster());
				bex.submitTask(solver);
			} catch (Exception e) {
				logger.error("", e);
				this.run = false;
			}
		}
		
		this.pingTimer.cancel();
		this.killAllThreads();

		UnicastRemoteObject.unexportObject(this, true);
		
		master = null;
		globalThreadPool.shutdownNow();
		logger.info("Slave thread shut down.");
		System.exit(0); // TODO: try to eliminate system.exit() by killing all rmi exports
	}
	
//	synchronized public static Slave instance() throws UnknownHostException {
//		if(instance == null) {
//			try {
//				instance = new Slave();
//			} catch (RemoteException e) {
//				logger.error("", e);
//				throw new RuntimeException();
//			} catch (InterruptedException e) {}
//		}
//		
//		return instance;
//	}
	
	
	
	public MasterRemote getMaster() {
		return master;
	}
	

	/**
	 * Program execution entry point
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		GetOpt go = new GetOpt(args, "");
		if(go.getCmdArgs().length > 0)
			new Slave(go.getCmdArgs()[0]);
		else
			new Slave();
	}

	public static void shutdownHost() {
		String shutdownCommand = "";
		String osName = System.getProperty("os.name");

		if (osName.startsWith("Win")) {
			shutdownCommand = "shutdown.exe -s -t 0";
		} else if (osName.startsWith("Linux") || osName.startsWith("Mac")) {
			shutdownCommand = "shutdown -h now";
		} else {
			logger.error("Shutdown unsupported operating system ...");
			System.exit(0);
		}

		try {
			Runtime.getRuntime().exec(shutdownCommand);
		} catch (IOException e) {
			logger.error(e);
		}
		System.exit(0);
	}

	public static void usage() {
		System.err.println("Usage: java main.java.Slave MASTERIP (ex. 192.168.1.10");
		System.exit(-1);
	}

	@Override
	public void abortFormula(String tqbfId) {
		logger.info("Received abort request for " + tqbfId);
		Solver threadToAbort = Solver.solvers.get(tqbfId);
		if(threadToAbort != null)
			threadToAbort.kill();
	}

//	@Override
//	public void computeFormula(TQbfRemote tqbf) throws RemoteException {
//		// this.tqbfs.add(tqbf);
//		// assert(tqbfs.size() <= this.getCores());
//		Solver solver = SolverFactory.getSolver(tqbf);
//		Slave.globalThreadPool.execute(solver);
//	}

	public void connect() {
		// if already connected clean up mess
		if (connected) {
			logger.info("Already connected. Reconnecting...");
			killAllThreads();
			// this.formulaListener.stop();
			connected = false;
		}

		while (!connected) {
			try {
				logger.info("Looking up " + masterRmiString + "...");
				this.master = (MasterRemote) Naming.lookup(masterRmiString);
				logger.info("Registering with Master...");
				getMaster().registerSlave(this);
				connected = true;
				break;
			} catch (MalformedURLException e) {
				logger.fatal("Wrong address? Exception was: ", e);
			} catch (RemoteException e) {
				logger.error("No response from master.");
			} catch (NotBoundException e) {
				logger.error("Something is not bound :P", e);
			} catch (UnknownHostException e) {
				logger.fatal("Wrong address?", e);
			}
			logger.info("Could not connect. Trying again in 5 seconds...");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public int getCores() throws RemoteException {
		return Configuration.getAvailableProcessors();
	}

	@Override
	public String getHostName() throws RemoteException, UnknownHostException {
		return hostname;
	}

	@Override
	public ArrayList<String> getSolvers() throws RemoteException {
		return availableSolvers;
	}

	@Override
	public void kill(String reason) throws RemoteException {
		logger.info("Killing slave...");
			
		this.run = false;
		
		
		//UnicastRemoteObject.unexportObject(this, true);
				
//		Runnable r = new Runnable() {
//			@Override
//			public void run() {
//				System.exit(0);
//			}
//		};
//		globalThreadPool.execute(r);
	}

	public void killAllThreads() {
		logger.info("Killing all threads...");
		for (Solver s : Solver.solvers.values()) {
			s.kill();
		}
	}

	@Override
	public void shutdown() throws RemoteException {
		logger.info("Shutting down host...");

		Runnable r = new Runnable() {
			@Override
			public void run() {
				Slave.shutdownHost();
			}
		};
		globalThreadPool.execute(r);
	}

	public String toString() {
		try {
			return "Slave -- Hostname: " + this.getHostName() + ", Solvers: " + this.getSolvers() + ", Cores: " + this.getCores();
		} catch (RemoteException e) {
			logger.error("RMI fail", e);
			return "";
		} catch (UnknownHostException e) {
			logger.error("Thar host is not known, arrr!", e);
			return "";
		}
	}

	@Override
	public int getRunningComputations() throws RemoteException {
		logger.info("We have " + Solver.solvers.size() + " running computations.");
		return Solver.solvers.size();
	}


}
