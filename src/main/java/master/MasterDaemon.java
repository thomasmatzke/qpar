package main.java.master;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.swing.ImageIcon;

import main.java.ArgumentParser;
import main.java.master.gui.JobsTableModel;
import main.java.master.gui.ProgramWindow;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.util.IndentPrinter;

/**
 * Handles communication with slaves
 * 
 * @author thomasm
 * 
 */
public class MasterDaemon {

	private static String user = ActiveMQConnection.DEFAULT_USER;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static Connection connection;
	private static BrokerService broker;
	private static ProgramWindow programWindow;

	public NewSlaveListener newSlaveListener;
	@SuppressWarnings("unused")
	private ShutdownHook hook;
	private static boolean startGui;

	public MasterDaemon() {
		startMessageBroker();
		hook = new ShutdownHook();
		connectToBroker();
		newSlaveListener = new NewSlaveListener();
		newSlaveListener.start();
		if (MasterDaemon.startGui) {
			programWindow = new ProgramWindow();
			programWindow.setVisible(true);
		}
	}

	public static void main(String[] args) {
		ArgumentParser ap = new ArgumentParser(args);
		String gui = ap.getOption("gui");
		if (gui == "false") {
			MasterDaemon.startGui = false;
		} else {
			MasterDaemon.startGui = true;
		}
		new MasterDaemon();
	}

	public static synchronized Connection getConnection() {
		if (connection != null) {
			return connection;
		}
		connectToBroker();
		return connection;
	}

	public static Session createSession() throws JMSException {
		return MasterDaemon.connection.createSession(true, 0);
	}

	public static void connectToBroker() {
		// Create the connection.
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				user, password, broker.getVmConnectorURI());
		try {
			connection = connectionFactory.createConnection();
			connection.start();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void startMessageBroker() {
		// Start Messagebroker for Slave-Communication
		try {
			broker = new BrokerService();
			broker.setUseJmx(false);
			broker.addConnector("tcp://localhost:61616");
			broker.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void stopMessageBroker() {
		try {
			broker.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class ShutdownHook extends Thread {
		@Override
		public void run() {
			try {
				ActiveMQConnection c = (ActiveMQConnection) connection;
				c.getConnectionStats().dump(new IndentPrinter());
				connection.close();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stopMessageBroker();
		}
	}

}
