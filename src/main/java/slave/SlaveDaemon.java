package main.java.slave;

import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.util.IndentPrinter;

import main.java.ArgumentParser;

/**
 * Handles communication with MasterDaemon
 * @author thomasm
 *
 */
public class SlaveDaemon {

	Hashtable<String, Tool> toolDirectory = new Hashtable<String, Tool>(); 
	public static String master_str;
	public static Master master;
	
	
	
	public static void main(String[] args) {
		ArgumentParser ap = new ArgumentParser(args);
		master_str = ap.nextParam();

		if (master_str == null) {
			usage();
		}
		master = new Master();
		
		try {
			master.connect(master_str);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			master.disconnect();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
		
	}
	
	public static void usage() {
		System.err.println("Usage: java main.java.Slave MASTER (ex. tcp://localhost:61616)");
		System.exit(-1);
	}
	
}
