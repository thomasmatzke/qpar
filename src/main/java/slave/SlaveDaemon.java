package main.java.slave;

import java.util.Hashtable;

import main.java.ArgumentParser;

/**
 * Handles communication with MasterDaemon
 * @author thomasm
 *
 */
public class SlaveDaemon {

	Hashtable<String, Tool> toolDirectory = new Hashtable<String, Tool>(); 
	public static String master;
	
	public static void main(String[] args) {
		ArgumentParser ap = new ArgumentParser(args);
		master = ap.nextParam();
		
		if (master == null) {
			usage();
		}
		
		
			
	}
	
	public static void usage() {
		System.err.println("Usage: java main.java.Slave MASTER");
		System.exit(-1);
	}
	
}
