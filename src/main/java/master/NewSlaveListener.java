package main.java.master;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import main.java.messages.InformationMessage;

public class NewSlaveListener implements MessageListener {

	private Session session;
	private Destination destination_reg;
	private boolean running;
	private MessageConsumer consumer;
		
	
	public boolean isRunning() {
		return running;
	}

	public void start() {
		running = true;
		try {
			this.session = MasterDaemon.createSession();
			destination_reg = this.session.createQueue("SLAVES.REGISTER.HERE");
			consumer = session.createConsumer(destination_reg);
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			session.close();
			running = false;
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMessage(Message m) {
		System.out.println("Rcvd msg...");
		if(!(m instanceof ObjectMessage)) {
			System.err.println("Received Non-ObjectMessage");
			return;
		}
		Object t = null;
		try {
			t = ((ObjectMessage)m).getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(t instanceof InformationMessage) {
			handleInformationMessage((InformationMessage)t);
		} else {
			System.err.println("Message object of unknown type.");
		}
	}
	
	public void handleInformationMessage(InformationMessage i) {
		System.out.println(i.getCores());
	}

}
