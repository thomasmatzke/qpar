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

import main.java.QPar;
import main.java.master.MasterDaemon;
import main.java.messages.AbortMessage;
import main.java.messages.ErrorMessage;
import main.java.messages.FormulaAbortedMessage;
import main.java.messages.FormulaMessage;
import main.java.messages.InformationMessage;
import main.java.messages.InformationRequestMessage;
import main.java.messages.KillMessage;
import main.java.messages.Ping;
import main.java.messages.Pong;
import main.java.messages.ResultMessage;
import main.java.messages.ShutdownMessage;
import main.java.slave.solver.QProSolver;
import main.java.slave.solver.Solver;
import main.java.slave.solver.SolverFactory;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.activemq.util.IndentPrinter;

/**
 * Represents the Master-server from client perspective
 * @author thomasm
 *
 */
public class Master {

	static Logger logger = Logger.getLogger(SlaveDaemon.class);
	
	private Connection connection;

	private MessageConsumer consumer_rcv;

	private Destination destination_rcv;

	private Destination destination_reg;

	private Destination destination_snd;

	private String password = ActiveMQConnection.DEFAULT_PASSWORD;

	private MessageProducer producer_reg;

	private MessageProducer producer_snd;

	private boolean run, connected;

	private Session session;

	private String user = ActiveMQConnection.DEFAULT_USER;

	public Master() {
		logger.setLevel(QPar.logLevel);
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Connects to the JMX Messagebroker.
	 * Creates the queues/consumer for communication with the master
	 * @param url
	 */
	public void connect(String url) {
		logger.info("Connecting to MessageBroker...");
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				user, password, url);
		try {
			while(!connected) {
				try {
					connection = connectionFactory.createConnection();
					connection.start();
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					connected = true;
				} catch (Exception e) {
					logger.error("Error while connecting to MessageBroker: \n"
							+ e);
					logger.error("Trying again in 5 secs...");
					Thread.sleep(5000);
				}
			}
			
			destination_rcv = session.createQueue("TO."
					+ InetAddress.getLocalHost().getHostName());
			destination_snd = session.createQueue("FROM."
					+ InetAddress.getLocalHost().getHostName());
			destination_reg = session.createQueue("SLAVES.REGISTER.HERE");
			producer_snd = session.createProducer(destination_snd);
			producer_reg = session.createProducer(destination_reg);
			producer_snd.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			producer_reg.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			consumer_rcv = session.createConsumer(destination_rcv);
		} catch (Exception e) {
			logger.error("Error while connecting to MessageBroker: \n"
					+ e);
			System.exit(-1);
		}
		try {
			sendInformationMessage(producer_reg);
		} catch (UnknownHostException e) {
			logger.error("Error while getting hostname: \n"	+ e);
			disconnect();
		}
		logger.info("Connection extablished. Queues, Consumers, Producers created.");
	}

	/**
	 * Disconnects from the MessageBroker
	 */
	public void disconnect() {
		logger.info("Disconnecting from MessageBroker, Stop consuming...");
		this.run = false;
		try {
			Thread.sleep(1000);
			this.session.close();
			this.connection.close();
		} catch (Exception e) {
			logger.error("Error while disconnecting from MessageBroker... \n"
					+ e);
		}
		logger.info("Disconnected from MessageBroker");
	}

	/**
	 * Stops the thread associated with the qbf in the message, 
	 * removes the thread from the thread-index and reports the successful 
	 * termination of the thread back to the master
	 * @param m
	 */
	private void handleAbortMessage(AbortMessage m) {
		logger.info("Received AbortMessage. TQbfId: " + m.getQbfId());
		Solver thread = SlaveDaemon.getThreads().get(m.getQbfId());
		if(thread != null)
			thread.kill();
		SlaveDaemon.getThreads().remove(m.getQbfId());
	}

	/**
	 * Receives a formula, and feeds it to the solver
	 * @param m
	 */
	private void handleFormulaMessage(FormulaMessage m) {
		logger.info("Received FormulaMessage. TQbfId: " + m.getFormula().getId());
		Solver solver = SolverFactory.getSolver(m.getSolver());
		solver.setTransmissionQbf(m.getFormula());
		solver.setMaster(this);
		solver.prepare();
		new Thread(solver).start();
		SlaveDaemon.addThread(m.getFormula().getId(), solver);
	}

	/**
	 * Sends basic information of the slave to the master. Currently only 
	 * the number of available cores
	 * @param m
	 * @throws UnknownHostException 
	 */
	private void handleInformationRequestMessage(InformationRequestMessage m) {
		logger.info("Received InformationRequestMessage.");
		try {
			this.sendInformationMessage();
		} catch (UnknownHostException e) {
			// Being here means at startup the Host was known, but now it isnt
			// Probably best to panic and get outta here...
			logger.error("Shutting down; Error while getting hostname: \n"	+ e);
			for (Solver t : SlaveDaemon.getThreads().values()) {
				t.kill();
			}
			this.run = false;
		}
	}

	/**
	 * Terminates the slave on request of the master
	 * Kills all threads first
	 * @param m
	 */
	private void handleKillMessage(KillMessage m) {
		logger.info("Received KillMessage. Killing Workerthreads...");
		
		for (Solver t : SlaveDaemon.getThreads().values()) {
			t.kill();
		}
		this.sendShutdownMessage("Kill requested by Masterserver");
		this.run = false;
	}

	/**
	 * This method is called whenever a message is received on
	 * the TO.<slave_name> queue.
	 * @param m
	 */
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
		logger.debug("Received message of type " + t.getClass().toString());
		if (t instanceof AbortMessage) {
			handleAbortMessage((AbortMessage) t);
		} else if (t instanceof FormulaMessage) {
			handleFormulaMessage((FormulaMessage) t);
		} else if (t instanceof InformationRequestMessage) {
			handleInformationRequestMessage((InformationRequestMessage) t);
		} else if (t instanceof KillMessage) {
			handleKillMessage((KillMessage) t);
		} else if (t instanceof Ping) {
			this.sendPong();
		} else {
			logger.error("Received message object of unknown type.");
		}
	}

	/**
	 * On completed termination of the threads associated with a qbf, this method
	 * sends a success-message to the master
	 * @param tqbfId
	 */
	public void sendFormulaAbortedMessage(String tqbfId) {
		logger.info("Sending AbortConfirmMessage... tqbfID: " + tqbfId);
		FormulaAbortedMessage msg = new FormulaAbortedMessage(tqbfId);
		sendObject(msg, producer_snd);
		logger.info("AbortConfirmMessage sent.");
	}

	/**
	 * Sends basic information of the slave to the master. Currently only 
	 * the number of available cores
	 * @throws UnknownHostException 
	 */
	public void sendInformationMessage() throws UnknownHostException {
		sendInformationMessage(producer_snd);
	}

	/**
	 * Sends an InformationMessage to the queue specified by the producer
	 * @param p The message producer to send the message to
	 * @throws UnknownHostException 
	 */
	public void sendInformationMessage(MessageProducer p) throws UnknownHostException {
		logger.info("Sending InformationMessage...");
		String hostname = InetAddress.getLocalHost().getHostName();
		InformationMessage msg = 
			new InformationMessage(	Runtime.getRuntime().availableProcessors(), 
									SlaveDaemon.availableSolvers, 
									hostname);
			
		sendObject(msg, p);
	}

	/**
	 * Sends an Object-Message to a queue represented by the message producer
	 * @param o	The object to send
	 * @param p The message producer the object is sent to
	 */
	private void sendObject(Serializable o, MessageProducer p) {
		try {
			logger.debug("Sending Object of Class : " + o.getClass() + " to " + p.getDestination());
			p.send(session.createObjectMessage(o));
		} catch (JMSException e) {
			logger.error("Error while sending object: \n" + e);
		}
	}

	/**
	 * Returns a result of a computation back to the master
	 * @param tqbfId
	 * @param result
	 */
	public void sendResultMessage(String tqbfId, boolean result) {
		logger.info("Sending ResultMessage... tqbfId: " + tqbfId + ", Result: "
				+ result);
		ResultMessage msg = new ResultMessage(tqbfId, result);
		sendObject(msg, producer_snd);
	}

	/**
	 * Tells the master that the slave is going to shutdown; transmitting 
	 * a list of open jobs and the reason for shutdown in the process
	 * @param reason
	 */
	public void sendShutdownMessage(String reason) {
		logger.info("Sending ShutdownMessage");
		ShutdownMessage msg = new ShutdownMessage(reason);
		this.sendObject(msg, producer_snd);
	}
	
	/**
	 * Sends a pong to the master, responding to a ping
	 */
	public void sendPong() {
		this.sendObject(new Pong(), producer_snd);
	}
	
	/**
	 * Sends an errormessage
	 * @param qbfId		The id of the qbf in which the error occurred
	 * @param message	the toString output of the error/exception
	 */
	public void sendErrorMessage(String qbfId, String message) {
		this.sendObject(new ErrorMessage(qbfId, message), producer_snd);
	}
	
	/**
	 * Starts consuming of messages from the TO.<slave_name> queue.
	 */
	public void startConsuming() {
		logger.info("Starting consuming from incoming queue");
		this.run = true;
		Message msg = null;
		while (run) {
			try {
				msg = consumer_rcv.receive(1000);
			} catch (JMSException e) {
				logger.error("Error while consuming Slavemessage...\n"
						+ e);
			}
			if (msg != null) {
				onMessage(msg);
			}
		}
		logger.info("Stopped consuming from incoming queue");
	}

	/**
	 * Stops consuming of messages from the TO.<slave_name> queue.
	 */
	public void stopConsuming() {
		logger.info("Stopping consuming from incoming queue");
		this.run = false;
	}

	

}
