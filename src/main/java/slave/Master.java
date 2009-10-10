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
import main.java.messages.FormulaAbortedMessage;
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

	static Logger logger = Logger.getLogger(SlaveDaemon.class);

	private Connection connection;

	private MessageConsumer consumer_rcv;

	private Destination destination_rcv;

	private Destination destination_reg;

	private Destination destination_snd;

	private String password = ActiveMQConnection.DEFAULT_PASSWORD;

	private MessageProducer producer_reg;

	private MessageProducer producer_snd;

	private boolean run;

	private Session session;

	private String user = ActiveMQConnection.DEFAULT_USER;

	public void connect(String url) {
		logger.info("Connecting to MessageBroker...");
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				user, password, url);
		try {
			boolean connected  = false;
			while(!connected) {
				try {
					connection = connectionFactory.createConnection();
					connection.start();
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					connected = true;
				} catch (Exception e) {
					logger.error("Error while connecting to MessageBroker: \n"
							+ e.getCause());
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
					+ e.getCause());
			System.exit(-1);
		}
		sendInformationMessage(producer_reg);
		logger.info("Connection extablished. Queues, Consumers, Producers created.");
	}

	public void disconnect() {
		logger.info("Disconnecting from MessageBroker, Stop consuming...");
		this.run = false;
		try {
			Thread.sleep(1000);
			this.session.close();
			this.connection.close();
		} catch (Exception e) {
			logger.error("Error while disconnecting from MessageBroker... \n"
					+ e.getStackTrace());
		}
		logger.info("Disconnected from MessageBroker");
	}

	private void handleAbortMessage(AbortMessage m) {
		Solver thread = SlaveDaemon.getThreads().get(m.getQbfId());
		thread.kill();
		SlaveDaemon.getThreads().remove(m.getQbfId());
		this.sendFormulaAbortedMessage(m.getQbfId());
	}

	private void handleFormulaMessage(FormulaMessage m) {
		QProSolver solver = new QProSolver();
		solver.setTransmissionQbf(m.getFormula());
		solver.setMaster(this);
		new Thread(solver).start();
		SlaveDaemon.addThread(m.getFormula().getId(), solver);
	}

	private void handleInformationRequestMessage(InformationRequestMessage m) {
		this.sendInformationMessage();
	}

	private void handleKillMessage(KillMessage m) {
		logger.info("Received KillMessage. Killing Workerthreads...");
		Hashtable<String, Solver> threads = SlaveDaemon.getThreads();
		for (Solver t : threads.values()) {
			t.kill();
		}
		Vector<String> qbf_ids = new Vector<String>();
		for (String t : SlaveDaemon.getThreads().keySet()) {
			qbf_ids.add(t);
		}
		this.sendShutdownMessage("Kill requested by Masterserver", qbf_ids);
		this.run = false;
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
			logger.error("Error while retrieving Object from Message... \n" + e.getStackTrace());
		}
		if (t instanceof AbortMessage) {
			handleAbortMessage((AbortMessage) t);
		} else if (t instanceof FormulaMessage) {
			handleFormulaMessage((FormulaMessage) t);
		} else if (t instanceof InformationRequestMessage) {
			handleInformationRequestMessage((InformationRequestMessage) t);
		} else if (t instanceof KillMessage) {
			handleKillMessage((KillMessage) t);
		} else {
			logger.error("Received message object of unknown type.");
		}
	}

	public void sendFormulaAbortedMessage(String tqbfId) {
		logger.info("Sending AbortConfirmMessage... tqbfID: " + tqbfId);
		FormulaAbortedMessage msg = new FormulaAbortedMessage();
		msg.setTqbfId(tqbfId);
		sendObject(msg, producer_snd);
		logger.info("AbortConfirmMessage sent.");
	}

	public void sendInformationMessage() {
		sendInformationMessage(producer_snd);
	}

	public void sendInformationMessage(MessageProducer p) {
		logger.info("Sending InformationMessage...");
		InformationMessage msg = new InformationMessage();
		msg.setCores(Runtime.getRuntime().availableProcessors());
		msg.setToolIds(SlaveDaemon.availableSolvers);
		try {
			msg.setHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			logger.error("Error while getting hostname: \n"	+ e.getCause());
		}
		sendObject(msg, p);
		logger.info("InformationMessage sent.");
	}

	private void sendObject(Serializable o, MessageProducer p) {
		try {
			p.send(session.createObjectMessage(o));
		} catch (JMSException e) {
			logger.error("Error while sending object: \n" + e.getStackTrace());
		}
	}

	public void sendResultMessage(String tqbfId, boolean result) {
		logger.info("Sending ResultMessage... tqbfId: " + tqbfId + ", Result: "
				+ result);
		ResultMessage msg = new ResultMessage();
		msg.setTqbfId(tqbfId);
		msg.setResult(result);
		sendObject(msg, producer_snd);
		logger.info("ResultMessage sent.");
	}

	public void sendShutdownMessage(String reason, Vector<String> open_jobs) {
		logger.info("Sending ShutdownMessage");
		ShutdownMessage msg = new ShutdownMessage();
		msg.setOpenJobs(open_jobs);
		msg.setReason(reason);
		this.sendObject(msg, producer_snd);
	}

	public void startConsuming() {
		logger.info("Starting consuming from incoming queue");
		this.run = true;
		Message msg = null;
		while (run) {
			try {
				msg = consumer_rcv.receive(1000);
			} catch (JMSException e) {
				logger.error("Error while consuming Slavemessage...\n"
						+ e.getStackTrace());
			}
			if (msg != null) {
				onMessage(msg);
			}
		}
		logger.info("Stopped consuming from incoming queue");
	}

	public void stopConsuming() {
		logger.info("Stopping consuming from incoming queue");
		this.run = false;
	}

}
