package main.java.slave;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import main.java.ArgumentParser;
import main.java.QPar;
import main.java.logic.TransmissionQbf;
import main.java.rmi.MasterRemote;
import main.java.rmi.SlaveRemote;
import main.java.slave.solver.Solver;
import main.java.slave.solver.SolverFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import sun.misc.Signal;

/**
 * Represents the slave-process.
 * Handles parameter-parsing, Signal-handling
 * @author thomasm
 *
 */
public class Slave extends UnicastRemoteObject implements SlaveRemote, Serializable  {
	static Logger logger = Logger.getLogger(Slave.class);
	public static ArrayList<String> availableSolvers = new ArrayList<String>();
	public static String masterIp;
	
	public boolean connected = false;
	public Hashtable<String, Solver> threads = new Hashtable<String, Solver>();
	public MasterRemote master = null;
		
	public Slave() throws InterruptedException, RemoteException {
		logger.info("Starting Slave...");
		SignalHandler handler = new SignalHandler(this);
		Signal.handle(new Signal("INT"), handler);
		Signal.handle(new Signal("TERM"), handler);
		//Signal.handle(new Signal("HUP"), handler);
					
		logger.info("Available Solvers are: " + availableSolvers);
		if (masterIp == null) {
			usage();
		}
				
		connect();
		
		new PingTimer(10, this);
		
		synchronized(this) {
			wait();
		}
		System.exit(0);
	}
		
	public void connect() throws InterruptedException {
		// Connect to master
		String masterName = "rmi://" + masterIp + ":1099/Master";
		
		while(!connected){
		
			try {
				master = (MasterRemote)Naming.lookup(masterName);
				master.registerSlave(this);
				connected = true;
				break;
			} catch (MalformedURLException e) {
				logger.fatal("Wrong address? Exception was: " + e);
				System.exit(-1);
			} catch (RemoteException e) {
				logger.error(e);
			} catch (NotBoundException e) {
				logger.error(e);
			} catch (UnknownHostException e) {
				logger.fatal("Wrong address? Exception was: " + e);
				System.exit(-1);
			}
			logger.info("Could not connect. Trying again in 5 seconds...");		
			Thread.sleep(5000);
		}
	}
	
	synchronized public void reconnect() {
		if(connected)
			return;
		killAllThreads();
		connected = false;
		logger.error("Reconnecting...");
		try {
			connect();
		} catch (InterruptedException e1) {}
	}
	
	/**
	 * Program execution entry point
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException, RemoteException {
		ArgumentParser ap = new ArgumentParser(args);
		// Basic console logging
		BasicConfigurator.configure();
		if(ap.hasOption("log")) {
			String lvl = ap.getOption("log");
			if(lvl.equals("debug"))
				QPar.logLevel = Level.DEBUG;
			else if(lvl.equals("info"))
				QPar.logLevel = Level.INFO;
			else
				usage();
		}
		Logger.getRootLogger().setLevel(QPar.logLevel);
		
		masterIp = ap.nextParam();
		String solversString = ap.getOption("solvers");
		if(solversString != null) {
			Scanner s = new Scanner(solversString).useDelimiter(",");
			while(s.hasNext()) {
				String cur = s.next();
				availableSolvers.add(cur);
			}
		} else {
			availableSolvers.add("qpro");
		}
		
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
	
	public void addThread(String qbfId, Solver solver) {
		synchronized(this.threads) {
			this.threads.put(qbfId, solver);
			try {
				assert(threads.size() <= this.getCores());
			} catch (RemoteException e) { logger.error(e); }
		}
	}

	@Override
	public int getCores() throws RemoteException {
		return Runtime.getRuntime().availableProcessors();
	}

	@Override
	public Hashtable<String, Solver> getThreads() {
		return threads;
	}
	
	@Override
	public String getHostName() throws RemoteException, UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}

	@Override
	public ArrayList<String> getSolvers() throws RemoteException {
		return availableSolvers;
	}

	@Override
	public synchronized void kill(String reason) throws RemoteException {
		logger.info("Killing slave...");
		new Thread() {
            @Override
			public void run() {
                 System.exit(0);
            }}.start();
	}

	@Override
	synchronized public void shutdown() throws RemoteException {
		logger.info("Shutting down host...");
		new Thread() {
            @Override
			public void run() {
                 Slave.shutdownHost();
            }}.start();
	}

	@Override
	public void computeFormula(TransmissionQbf formula, String solverId)
			throws RemoteException {
		logger.info("Starting computation of formula " + formula.getId());
		Solver s = SolverFactory.getSolver(solverId, this);
		s.setTransmissionQbf(formula);
		synchronized(threads) {
			threads.put(formula.getId(), s);
			s.getThread().start();
		}
	}

	@Override
	public void abortFormula(String tqbfId) {
		logger.info("Aborting formula " + tqbfId);
		synchronized(threads) {
			Solver s = threads.get(tqbfId);
			if(s == null)
				return;			
			s.kill();
			threads.remove(tqbfId);
		}
	}

	@Override
	public String[] getCurrentJobs() throws RemoteException {
		ArrayList<String> jobIds = new ArrayList<String>();
		synchronized(threads) {
			for(Solver s : threads.values()) {
				if(!jobIds.contains(s.getTransmissionQbf().jobId)) {
					jobIds.add(s.getTransmissionQbf().jobId);
				}
			}
		}
		return jobIds.toArray(new String[jobIds.size()]);
	}
	
	public void killAllThreads() {
		synchronized(threads) {
			for(Solver s : threads.values()) {
				s.kill();
			}
			threads = new Hashtable<String, Solver>();
		}
	}
	
	public String toString() {
		try {
			return "Slave -- Hostname: " + this.getHostName() + ", Solvers: "
					+ this.getSolvers()	+ ", Cores: " + this.getCores();
		} catch (RemoteException e) {
			logger.error(e);
			return "";
		} catch (UnknownHostException e) {
			logger.error(e);
			return "";
		}
	}

	@Override
	public void setMailInfo(String mailServer, String mailUser, String mailPass) {
		QPar.mailServer = mailServer;
		QPar.mailUser = mailUser;
		QPar.mailPass = mailPass;
	}

	@Override
	public void setExceptionNotifierAddress(String address)
			throws RemoteException {
		QPar.exceptionNotifierAddress = address;
	}
	
}
