package main.java.slave;

import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

import main.java.ArgumentParser;
import main.java.slave.solver.Solver;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import sun.misc.Signal;

/**
 * Handles communication with MasterDaemon
 * @author thomasm
 *
 */
public class SlaveDaemon {

	public static Vector<String> availableSolvers = new Vector<String>();
	public static String master_str;
	public static Master master;
	private static Hashtable<String, Solver> threads = new Hashtable<String, Solver>();
	static Logger logger = Logger.getLogger(SlaveDaemon.class);
	
	public static void main(String[] args) {
		// Basic console logging
		BasicConfigurator.configure();
		logger.info("Starting Slave...");
		SignalHandler handler = new SignalHandler();
		Signal.handle(new Signal("INT"), handler);
		Signal.handle(new Signal("TERM"), handler);
		//Signal.handle(new Signal("HUP"), handler);
		ArgumentParser ap = new ArgumentParser(args);
		master_str = ap.nextParam();
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
		
		logger.info("Available Solvers are: " + availableSolvers);
		if (master_str == null) {
			usage();
		}
		master = new Master();
		
		
		master.connect(master_str);
		master.startConsuming();
		master.disconnect();
		
		System.exit(0);
		
	}
	
	public static void usage() {
		System.err.println("Usage: java main.java.Slave MASTER (ex. tcp://localhost:61616)");
		System.exit(-1);
	}

	public static Hashtable<String, Solver> getThreads() {
		return threads;
	}
	
	public static void addThread(String qbfId, Solver solver) {
		threads.put(qbfId, solver);
	}
	
}
