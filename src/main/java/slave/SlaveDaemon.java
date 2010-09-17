package main.java.slave;

import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

import main.java.ArgumentParser;
import main.java.QPar;
import main.java.master.MasterDaemon;
import main.java.slave.solver.Solver;

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
public class SlaveDaemon {
	static Logger logger = Logger.getLogger(SlaveDaemon.class);
	
	public static Vector<String> availableSolvers = new Vector<String>();
	public static String master_str;
	public static Master master;
	public static Hashtable<String, Solver> threads = new Hashtable<String, Solver>();
	
	/**
	 * Program execution entry point
	 * @param args
	 */
	public static void main(String[] args) {
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
		logger.setLevel(QPar.logLevel);
		logger.info("Starting Slave...");
		SignalHandler handler = new SignalHandler();
		Signal.handle(new Signal("INT"), handler);
		Signal.handle(new Signal("TERM"), handler);
		//Signal.handle(new Signal("HUP"), handler);
		
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
