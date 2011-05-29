package qpar.master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.table.AbstractTableModel;


import org.apache.log4j.Logger;

import qpar.common.ArgumentParser;
import qpar.common.Configuration;
import qpar.common.rmi.MasterRemote;
import qpar.common.rmi.SlaveRemote;
import qpar.common.rmi.TQbfRemote;
import qpar.master.console.Shell;
import qpar.master.gui.ProgramWindow;


/**
 * Handles communication with slaves
 * 
 * @author thomasm
 * 
 */
public class Master extends UnicastRemoteObject implements MasterRemote, Serializable {

	private static final long serialVersionUID = -6189223346936131655L;

	private static ArgumentParser ap;

	static Logger logger = Logger.getLogger(Master.class);
	
	private static ProgramWindow programWindow;
	transient private Shell shell;
	private static boolean startGui = false;
	private Registry registry = null;
	
	public static AbstractTableModel slaveTableModel;
	
	public static ConcurrentHashMap<String, Boolean> resultCache = new ConcurrentHashMap<String, Boolean>();
	public static ExecutorService globalThreadPool = Executors.newCachedThreadPool();
	private int cacheHits = 0, cacheMisses = 0;
	
	public Master() throws FileNotFoundException, RemoteException, NotBoundException {		
		
		// Start the registry
		try {
			registry = LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			logger.fatal(e);
			throw e;
		}
		
		// Start own interface
		MasterRemote myInterface = this;
		registry.rebind("Master", myInterface);
		
		if (Master.startGui) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					programWindow = new ProgramWindow();
					programWindow.setVisible(true);
				}
			});
		} else {
			if (ap.hasOption("i")) {
				shell = new Shell(new BufferedReader(new FileReader(ap
						.getOption("i"))));
				
			} else {
				shell = new Shell();
			}

			shell.run();
		}
		System.exit(0);
	}

	private static void usage() {
		System.out
				.println("Arguments: \"-gui\"               toggles graphical user interface"
						+ "           \"-i=INPUTFILE\"       specifies a batch-file");
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

		ap = new ArgumentParser(args);
		Master.startGui = ap.hasOption("gui");
				
		Configuration.loadConfig();
		
		Master.globalThreadPool.execute(new MulticastBeacon());
		try {
			new Master();
		} catch (Throwable t) {
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
	public TQbfRemote getWork() throws RemoteException {
		logger.info("Returning work unit");
		return Distributor.instance().getWork();
	}
	
}
