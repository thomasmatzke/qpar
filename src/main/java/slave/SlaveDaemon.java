package main.java.slave;

import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

import javax.jms.JMSException;

import main.java.ArgumentParser;
import main.java.slave.solver.Solver;
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
		
	public static void main(String[] args) {
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
		
		if (master_str == null) {
			usage();
		}
		master = new Master();
		
		try {
			master.connect(master_str);
			master.startConsuming();
			shutdown();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
		
	}
	
	public static void shutdown() {
		master.disconnect();
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
