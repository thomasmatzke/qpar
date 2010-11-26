package main.java.master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import main.java.ArgumentParser;
import main.java.QPar;
import main.java.master.Console.Shell;
import main.java.master.gui.ProgramWindow;
import main.java.rmi.MasterRemote;
import main.java.rmi.Result;
import main.java.rmi.SlaveRemote;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Handles communication with slaves
 * 
 * @author thomasm
 * 
 */
public class Master implements MasterRemote, Serializable {

	private static ArgumentParser ap;

	static Logger logger = Logger.getLogger(Master.class);
	
	private static ProgramWindow programWindow;
	private static Thread shellthread;
	private static Shell shell;

	private static boolean startGui = false;
	
	private static HashMap<String, SlaveRemote> slaves = new HashMap<String, SlaveRemote>();

	public static AbstractTableModel slaveTableModel;
	
	public static void removeSlave(SlaveRemote slave) throws RemoteException, UnknownHostException {
		slaves.remove(slave.getHostName());
		if (slaveTableModel != null) {
			slaveTableModel.fireTableDataChanged();
		}
	}
	
	public static void addSlave(SlaveRemote slave) throws RemoteException, UnknownHostException {
		slaves.put(slave.getHostName(), slave);
		logger.debug("Adding Slave: " + slave);
		if (slaveTableModel != null) {
			slaveTableModel.fireTableDataChanged();
		}
	}
	
	public static HashMap<String, SlaveRemote> getSlaves() {
		return slaves;
	}
	
	public static void main(String[] args) {

		// Basic console logging
		BasicConfigurator.configure();

		ap = new ArgumentParser(args);
		Master.startGui = ap.hasOption("gui");
		if (ap.hasOption("log")) {
			String lvl = ap.getOption("log");
			if (lvl.equals("debug"))
				QPar.logLevel = Level.DEBUG;
			else if (lvl.equals("info"))
				QPar.logLevel = Level.INFO;
			else
				usage();
		}
		logger.setLevel(QPar.logLevel);
		new Master();
	}

	public Master() {
		Registry registry = null;
		// Start the registry
		try {
			registry = LocateRegistry.createRegistry(1099);
		} catch (RemoteException e1) {
			logger.fatal(e1);
			System.exit(-1);
		}
		
		// Start own interface
		MasterRemote myInterface = this;
		try {
			UnicastRemoteObject.exportObject(this, 0);
			registry.rebind("Master", myInterface);
		} catch (RemoteException e) {
			logger.fatal(e);
			System.exit(-1);
		}
		
		if (Master.startGui) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					programWindow = new ProgramWindow();
					programWindow.setVisible(true);
				}
			});
		} else {
			if (ap.hasOption("i")) {
				try {
					shell = new Shell(new BufferedReader(new FileReader(ap
							.getOption("i"))));
				} catch (FileNotFoundException e) {
					logger.fatal(e);
					System.exit(-1);
				}
			} else {
				shell = new Shell();
			}

			shellthread = new Thread(shell);
			shellthread.start();
		}
	}

	private static void usage() {
		System.out
				.println("Arguments: \"-gui\"               toggles graphical user interface"
						+ "           \"-i=INPUTFILE\"       specifies a batch-file"
						+ "           \"-log=(debug|info)\"  specifies log-lvl");
		System.exit(-1);
	}

	public static Shell getShell() {
		return shell;
	}

	public static Thread getShellThread() {
		return shellthread;
	}

	@Override
	public void unregisterSlave(SlaveRemote slave) throws RemoteException, UnknownHostException {
		logger.info("Unregistering Slave. Hostname: " + slave.getHostName());
		for(String jobId : slave.getCurrentJobs()) {
			Job j = Job.getJobs().get(jobId);
			if(j.getStatus() == Job.Status.RUNNING)
				j.abort();
		}
	}

	@Override
	public void registerSlave(SlaveRemote ref) throws RemoteException, UnknownHostException {
		logger.info("Registering Slave. Hostname: " + ref.getHostName() + ", Cores: " + ref.getCores() + ", Solvers: " + ref.getSolvers());
		Master.addSlave(ref);
		synchronized(Master.getShellThread()) {
			Master.getShellThread().notify();
		}
	}

	@Override
	public void returnResult(Result r) throws RemoteException {
		logger.info("Result returned. Job: " + r.jobId + ", tqbfId: " + r.tqbfId + ", ResultType: " + r.type.toString());
		Job.getJobs().get(r.jobId).handleResult(r);
	}

	public static int getCoresWithSolver(String solver) throws RemoteException {
		int c = 0;
		for(SlaveRemote s : Master.getSlaves().values()) {
			if(s.getSolvers().contains(solver))
				c += s.getCores();
		}
		return c;
	}

	public static ArrayList<SlaveRemote> getSlavesWithSolver(String solver) throws RemoteException {
		ArrayList<SlaveRemote> slaves = new ArrayList<SlaveRemote>();
		for(SlaveRemote s : Master.getSlaves().values()) {
			if(s.getSolvers().contains(solver))
				slaves.add(s);
		}
		return slaves;
	}

	public static Set<String> getAllAvaliableSolverIds() throws RemoteException {
		Set<String> solverIds = new HashSet<String>();
		for(SlaveRemote s : Master.getSlaves().values()) {
			solverIds.addAll(s.getSolvers());
		}
		return solverIds;
	}

	@Override
	public void ping() throws RemoteException {}

}