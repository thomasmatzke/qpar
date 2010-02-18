package main.java.master;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.log4j.Logger;

import main.java.messages.InformationMessage;

public class NewSlaveListener implements MessageListener {

	private MessageConsumer consumer;
	private Destination destination_reg;
	private boolean running;
	private Session session;
	
	static Logger logger = Logger.getLogger(MasterDaemon.class);

	public void handleInformationMessage(InformationMessage i) {
		logger.info("Handling InformationMessage...");
		Slave.create(i.getHostName(), i.getCores(), i.getToolIds());
		logger.info("InformationMessage handled.");
	}

	public boolean isRunning() {
		return running;
	}

	public void onMessage(Message m) {
		if (!(m instanceof ObjectMessage)) {
			logger.error("Received unknown Non-Object-Message. Ignoring");
			return;
		}
		Object t = null;
		try {
			t = ((ObjectMessage) m).getObject();
		} catch (JMSException e) {
			logger.error("Error while retrieving Object from Message... \n" + e);
		}
		if (t instanceof InformationMessage) {
			logger.info("Received InformationMessage...");
			handleInformationMessage((InformationMessage) t);
		} else {
			logger.error("Received message object of unknown type.");
		}
	}

	public void start() {
		logger.info("Starting NewSlaveListener...");
		running = true;
		try {
			this.session = MasterDaemon.createSession();
			destination_reg = this.session.createQueue("SLAVES.REGISTER.HERE");
			consumer = session.createConsumer(destination_reg);
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			logger.error("Error while starting NewSlaveListener: \n" + e);
		}
		logger.info("NewSlaveListener started.");
	}

	public void stop() {
		logger.info("Stopping NewSlaveListener...");
		try {
			session.close();
			running = false;
		} catch (JMSException e) {
			logger.error("Error while stopping NewSlaveListener: \n" + e);
		}
		logger.info("NewSlaveListener stopped.");
	}

}
