package main.java.master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import main.java.ArgumentParser;
import main.java.master.Console.Shell;
import main.java.master.gui.ProgramWindow;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.util.IndentPrinter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Handles communication with slaves
 * 
 * @author thomasm
 * 
 */
public class MasterDaemon {

	private static class ShutdownHook extends Thread {
		@Override
		public void run() {
			try {
				for(Slave slave : Slave.getSlaves().values()){
					slave.stop();
				}
				connection.close();
			} catch (JMSException e) {
				logger.error("Error while closing connection: \n"
						+ e);
			}
			stopMessageBroker();
		}
	}

	private static ArgumentParser ap;
	
	private static BrokerService broker;

	private static Connection connection;
	static Logger logger = Logger.getLogger(MasterDaemon.class);
	public static Level logLevel = Level.WARN;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static ProgramWindow programWindow;
	private static Thread shellthread;
	private static Shell shell;

	private static boolean startGui = false;
	private static String user = ActiveMQConnection.DEFAULT_USER;

	public static void connectToBroker() {
		logger.info("Connecting to MessageBroker...");
		// Create the connection.
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				user, password, broker.getVmConnectorURI());
		try {
			connection = connectionFactory.createConnection();
			connection.start();
		} catch (JMSException e) {
			logger.error("Could not establish connection to MessageBroker: \n" + e);
		}
		logger.info("Connection to MessageBroker established.");
	}

	public static Session createSession() throws JMSException {
		return MasterDaemon.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	public static synchronized Connection getConnection() {
		if (connection != null) {
			return connection;
		}
		connectToBroker();
		return connection;
	}

	public static void main(String[] args) {

		// Basic console logging
		BasicConfigurator.configure();

		ap = new ArgumentParser(args);
		MasterDaemon.startGui = ap.hasOption("gui");
		
		new MasterDaemon();
	}

	public static void startMessageBroker() {
		logger.info("Starting Messagebroker...");
		// Start Messagebroker for Slave-Communication
		try {
			broker = new BrokerService();
			broker.setUseJmx(false);
			broker.addConnector("tcp://localhost:61616");
			broker.start();
		} catch (Exception e) {
			logger.error("Error while starting MessageBroker: \n" + e);
		}
		logger.info("Messagebroker started.");
	}

	public static void stopMessageBroker() {
		logger.info("Stopping Messagebroker...");
		try {
			broker.stop();
		} catch (Exception e) {
			logger.error("Error while stopping MessageBroker: \n" + e);
		}
		logger.info("Messagebroker stopped.");
	}

	@SuppressWarnings("unused")
	private ShutdownHook hook;

	public NewSlaveListener newSlaveListener;

	public MasterDaemon() {
		if(ap.hasOption("log")) {
			String lvl = ap.getOption("log");
			if(lvl.equals("debug"))
					MasterDaemon.logLevel = Level.DEBUG;
			else if(lvl.equals("info"))
				MasterDaemon.logLevel = Level.INFO;
			else
				usage();
		}
		logger.setLevel(logLevel);
		startMessageBroker();
		hook = new ShutdownHook();
		Runtime.getRuntime().addShutdownHook(hook);
		connectToBroker();
		newSlaveListener = new NewSlaveListener();
		newSlaveListener.start();
		if (MasterDaemon.startGui) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					programWindow = new ProgramWindow();
					programWindow.setVisible(true);
				}
			});
		} else {
			if(ap.hasOption("i")) {
				try {
					shell = new Shell(new BufferedReader(new FileReader(ap.getOption("i"))));
				} catch (FileNotFoundException e) {
					logger.error(e);
				}
			} else {
				shell = new Shell();
			}
						
			shellthread = new Thread(shell);
			shellthread.start();
		}
	}

	private static void usage() {
		System.out.println(	"Arguments: \"-gui\"               toggles graphical user interface" +
							"           \"-i=INPUTFILE\"       specifies a batch-file" +
							"           \"-log=(debug|info)\"  specifies log-lvl");
		System.exit(-1);
	}

	public static Shell getShell() {
		return shell;
	}
	
	public static Thread getShellThread() {
		return shellthread;
	}

}
