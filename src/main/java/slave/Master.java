package main.java.slave;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import main.java.messages.InformationMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;

public class Master {
	
	private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private Connection connection;
    private Session session;
    private Destination destination_rcv;
    private Destination destination_snd;
    private Destination destination_reg;
    private MessageProducer producer_snd;
    private MessageProducer producer_reg;
    
    
    public void connect(String url) throws JMSException, UnknownHostException {
    	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, 1);
        destination_rcv = session.createQueue("TO." + InetAddress.getLocalHost().getHostAddress());
        destination_snd = session.createQueue("FROM." + InetAddress.getLocalHost().getHostAddress());
        destination_reg = session.createQueue("SLAVES.REGISTER.HERE");
        producer_snd = session.createProducer(destination_snd);
        producer_reg = session.createProducer(destination_reg);
        producer_snd.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer_reg.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        sendInformationMessage(producer_reg);
                
    }
    
    public void disconnect() throws JMSException {
    	this.session.close();
    	this.connection.close();
    }
    
    public void sendAbortConfirmMessage() {
    	
    }
    
    public void sendInformationMessage() {
    	sendInformationMessage(producer_snd);
    }
    
    public void sendInformationMessage(MessageProducer p) {
    	InformationMessage msg = new InformationMessage();
    	msg.setCores(Runtime.getRuntime().availableProcessors());
    	sendObject(msg, p);
    	try {
			session.commit();
		} catch (JMSException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    }
    
    public void sendResultMessage() {
    	
    }
    
    private void sendObject(Serializable o, MessageProducer p) {
    	try {
			p.send(session.createObjectMessage(o));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}
