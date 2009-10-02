package main.java.slave;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import main.java.master.MasterDaemon;
import main.java.messages.AbortConfirmMessage;
import main.java.messages.AbortMessage;
import main.java.messages.FormulaMessage;
import main.java.messages.InformationMessage;
import main.java.messages.InformationRequestMessage;
import main.java.messages.KillMessage;
import main.java.messages.ResultMessage;
import main.java.messages.ShutdownMessage;
import main.java.messages.SlaveShutdownMessage;
import main.java.slave.solver.QProSolver;
import main.java.slave.solver.Solver;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

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
    private MessageConsumer consumer_rcv;
    private boolean running;
    static Logger logger = Logger.getLogger(MasterDaemon.class);
    
    public void connect(String url) throws JMSException, UnknownHostException {
    	logger.info("Connecting to MessageBroker...");
    	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, 1);
        destination_rcv = session.createQueue("TO." + InetAddress.getLocalHost().getHostName());
        destination_snd = session.createQueue("FROM." + InetAddress.getLocalHost().getHostName());
        destination_reg = session.createQueue("SLAVES.REGISTER.HERE");
        producer_snd = session.createProducer(destination_snd);
        producer_reg = session.createProducer(destination_reg);
        producer_snd.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer_reg.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        consumer_rcv = session.createConsumer(destination_rcv);
        sendInformationMessage(producer_reg);
        logger.info("Connection extablished. Queues, Consumers, Producers created.");
    }
    
    public void disconnect(){
    	logger.info("Disconnecting from MessageBroker, Stop consuming...");
    	this.running = false;
    	try {
			Thread.sleep(1000);
	    	this.session.close();
	    	this.connection.close();
    	} catch (Exception e) {
    		logger.error("Error while disconnecting from MessageBroker... \n" + e.getStackTrace());
		}
    	logger.info("Disconnected from MessageBroker");
    }
    
    public void startConsuming() {
    	logger.info("Starting consuming from incoming queue");
    	this.running = true;
    	Message msg = null;
    	while(running) {
    		try {
				msg = consumer_rcv.receive(1000);
			} catch (JMSException e) {
				logger.error("Error while consuming Slavemessage...\n" + e.getStackTrace());
			}
			if(msg != null) {
				onMessage(msg);
			}
    	}
    	logger.info("Stopped consuming from incoming queue");
    }
    
    public void stopConsuming() {
    	logger.info("Stopping consuming from incoming queue");
    	this.running = false;
    }
    
    public void sendSlaveShutdownMessage(String reason) {
    	SlaveShutdownMessage msg = new SlaveShutdownMessage();
    	msg.setReason(reason);
    	this.sendObject(msg, producer_snd);
    }
    
    public void sendAbortConfirmMessage(String jobId) {
    	AbortConfirmMessage msg  = new AbortConfirmMessage();
    	msg.setTqbfId(jobId);
    	sendObject(msg, producer_snd);
    }
    
    public void sendInformationMessage() {
    	sendInformationMessage(producer_snd);
    }
    
    public void sendInformationMessage(MessageProducer p) {
    	InformationMessage msg = new InformationMessage();
    	msg.setCores(Runtime.getRuntime().availableProcessors());
    	msg.setToolIds(SlaveDaemon.availableSolvers);
    	try {
			msg.getHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	sendObject(msg, p);
    }
    
    public void sendResultMessage(String tqbfId, boolean result) {
    	ResultMessage msg = new ResultMessage();
    	msg.setTqbfId(tqbfId);
    	msg.setResult(result);
    	sendObject(msg, producer_snd);
    }
    
    public void sendShutdownMessage(String reason, Vector<String> open_jobs) {
    	ShutdownMessage msg = new ShutdownMessage();
    	msg.setOpenJobs(open_jobs);
    	msg.setReason(reason);
    	this.sendObject(msg, producer_snd);
    }
    
    private void sendObject(Serializable o, MessageProducer p) {
    	try {
			p.send(session.createObjectMessage(o));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public void onMessage(Message m) {
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
		if(t instanceof AbortMessage) {
			handleAbortMessage((AbortMessage) m);
		} else if(t instanceof FormulaMessage) {
			handleFormulaMessage((FormulaMessage) m);
		} else if(t instanceof InformationRequestMessage) {	
			handleInformationRequestMessage((InformationRequestMessage) m);
		} else if(t instanceof KillMessage) {
			handleKillMessage((KillMessage) m);
		} else {
			System.err.println("Message object of unknown type.");
		}
	}
	
	private void handleAbortMessage(AbortMessage m) {
		Solver thread = SlaveDaemon.getThreads().get(m.getQbfId());
		thread.kill();
		SlaveDaemon.getThreads().remove(m.getQbfId());
		this.sendAbortConfirmMessage(m.getQbfId());
	}
	
	private void handleFormulaMessage(FormulaMessage m) {
		QProSolver solver = new QProSolver();
		solver.setTransmissionQbf(m.getFormula());
		new Thread(solver).start();
		SlaveDaemon.addThread(m.getFormula().getId(), solver);
	}
	
	private void handleInformationRequestMessage(InformationRequestMessage m) {
		this.sendInformationMessage();
	}
	
	private void handleKillMessage(KillMessage m) {
		Hashtable<String, Solver> threads = SlaveDaemon.getThreads();
		for(Solver t : threads.values()) {
			t.kill();
		}
		Vector<String> qbf_ids = new Vector<String>();
		for(String t : SlaveDaemon.getThreads().keySet()) {
			qbf_ids.add(t);
		}
		this.sendShutdownMessage("Kill requested by Masterserver", qbf_ids);
		this.disconnect();
		SlaveDaemon.shutdown();
	}
	
}
