package main.java.slave;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import main.java.ArgumentParser;
import main.java.QPar;
import main.java.rmi.MasterRemote;
import main.java.rmi.SlaveRemote;

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
public class Slave extends UnicastRemoteObject implements SlaveRemote  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7927545942720427850L;
	static Logger logger = Logger.getLogger(Slave.class);
	public static ArrayList<String> availableSolvers = new ArrayList<String>();
	public String masterIp;
	
	public boolean connected = false;
//	public Hashtable<String, Solver> threads = new Hashtable<String, Solver>();
	public static MasterRemote master = null;
	
	public FormulaListener formulaListener = null;
	
	public Slave(String masterIp) throws InterruptedException, RemoteException {
		//super(0, new ZipClientSocketFactory(), new ZipServerSocketFactory() );
		this.masterIp = masterIp;
		logger.info("Starting Slave...");
				
		QPar.loadConfig();
	
		SignalHandler handler = new SignalHandler(this);
		Signal.handle(new Signal("INT"), handler);
		Signal.handle(new Signal("TERM"), handler);
		//Signal.handle(new Signal("HUP"), handler);
		
		try {
			this.formulaListener = new FormulaListener(11111);
			new Thread(this.formulaListener).start();
		} catch (IOException e) {
			logger.fatal(e);
			System.exit(-1);
		}
		
		logger.info("Available Solvers are: " + availableSolvers);
		if (masterIp == null) {
			// so no ip was set...listen for the beacon...
			try {
				new Thread(new BeaconListener(this)).start();
				// Wait til we found a signal
				synchronized(this) { wait(); }
			} catch (UnknownHostException e) {
				logger.error("Unknown host", e);
			} catch (IOException e) {
				logger.error("Cant start beaconlistener", e);
			}
		}
		
		connect();
		
		new PingTimer(10, this);
		
		synchronized(this) { wait(); }
		System.exit(0);
	}
		
	

	public void connect() throws InterruptedException {
		// Connect to master
		String masterName = "rmi://" + masterIp + ":1099/Master";
		
		while(!connected){
		
			try {
				logger.info("Looking up " + masterName + "...");
				master = (MasterRemote)Naming.lookup(masterName);
				logger.info("Registering with Master...");
				master.registerSlave(this);
				connected = true;
				break;
			} catch (MalformedURLException e) {
				logger.fatal("Wrong address? Exception was: " + e);
				System.exit(-1);
			} catch (RemoteException e) {
				logger.error("RMI fail", e);
			} catch (NotBoundException e) {
				logger.error("Something is not bound :P", e);
			} catch (UnknownHostException e) {
				logger.fatal("Wrong address?", e);
				System.exit(-1);
			}
			logger.info("Could not connect. Trying again in 5 seconds...");		
			Thread.sleep(5000);
		}
	}
	
	synchronized public void reconnect() {
		connected = false;
		logger.warn("Killing threads...");
		killAllThreads();
		this.formulaListener.stop();
		try {
			this.formulaListener = new FormulaListener(11111);
			new Thread(this.formulaListener).start();
		} catch (IOException e) {
			logger.fatal(e);
			System.exit(-1);
		}
		logger.warn("Reconnecting...");
		try {
			connect();
		} catch (InterruptedException e1) {}
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
	public int getCores() throws RemoteException {
		return Runtime.getRuntime().availableProcessors();
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
	public void kill(String reason) throws RemoteException {
		logger.info("Killing slave...");
		this.killAllThreads();
		new Thread() {
            @Override
			public void run() {
                 System.exit(0);
            }}.start();
	}

	@Override
	public void shutdown() throws RemoteException {
		logger.info("Shutting down host...");
		new Thread() {
            @Override
			public void run() {
                 Slave.shutdownHost();
            }}.start();
	}

	@Override
	public void abortFormula(String tqbfId) {
		ComputationStateMachine computation = ComputationStateMachine.computations.get(tqbfId);
		if(computation != null)
			computation.abortComputation();
	}

	@Override
	public String[] getCurrentJobs() throws RemoteException {
		Set<String> jobIds = new HashSet<String>();
		for(String tqbfId : ComputationStateMachine.computations.keySet()) {
			String jobPrefix = tqbfId.split("\\.")[0];
			jobIds.add(jobPrefix);
		}
		return jobIds.toArray(new String[jobIds.size()]);
	}
	
	public void killAllThreads() {
		logger.info("Killing all threads...");
		for(ComputationStateMachine machine : ComputationStateMachine.computations.values()) {
			machine.abortComputation();
			logger.info("Computation " + machine.tqbfId + " aborted.");
		}
	}
	
	public String toString() {
		try {
			return "Slave -- Hostname: " + this.getHostName() + ", Solvers: "
					+ this.getSolvers()	+ ", Cores: " + this.getCores();
		} catch (RemoteException e) {
			logger.error("RMI fail", e);
			return "";
		} catch (UnknownHostException e) {
			logger.error("Thar host is not known, arrr!", e);
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
				
		String solversString = ap.getOption("solvers");
		if(solversString != null) {
			Scanner s = new Scanner(solversString).useDelimiter(",");
			while(s.hasNext()) {
				String cur = s.next();
				availableSolvers.add(cur);
			}
		} else {
			availableSolvers.add("qpro");
			availableSolvers.add("simple");
		}
		
		new Slave(ap.nextParam());
					
	}
	
}
