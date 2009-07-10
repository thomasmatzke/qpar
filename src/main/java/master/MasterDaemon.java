package main.java.master;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

/**
 * Handles communication with slaves
 * @author thomasm
 *
 */
public class MasterDaemon {

	private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private Connection connection;
	
	public BrokerService broker;
	
	public MasterDaemon() {
		// Start Messagebroker for Slave-Communication
		try {
			broker = new BrokerService();
	        broker.setUseJmx(true);
			broker.addConnector("tcp://localhost:61616");
	        broker.start();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // Create the connection.
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, broker.getVmConnectorURI());
        try {
			connection = connectionFactory.createConnection();
			connection.start();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        // Start a broadcast
	}
		
	public static void main(String[] args) {
		new MasterDaemon();
	}
	
}
