package main.java.master;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.swing.table.AbstractTableModel;

import main.java.logic.TransmissionQbf;
import main.java.messages.AbortConfirmMessage;
import main.java.messages.AbortMessage;
import main.java.messages.FormulaMessage;
import main.java.messages.InformationMessage;
import main.java.messages.InformationRequestMessage;
import main.java.messages.KillMessage;
import main.java.messages.ResultMessage;
import main.java.messages.ShutdownMessage;

import org.apache.log4j.Logger;

public class Slave implements MessageListener {
	static Logger logger = Logger.getLogger(MasterDaemon.class);
	private static Vector<Slave> slaves = new Vector<Slave>();
	private static AbstractTableModel tableModel;

	private static void addSlave(Slave slave) {
		slaves.add(slave);
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	public static Slave create(String hostName) throws JMSException {
		logger.info("Starting new Slaveinstance/thread...");
		Slave instance = new Slave();
		instance.start();
		Slave.addSlave(instance);
		return instance;
	}

	public static Set<String> getAllAvaliableSolverIds() {
		Set<String> allSolvers = new HashSet<String>();
		for (Slave slave : slaves) {
			for (String id : slave.getToolIds()) {
				allSolvers.add(id);
			}
		}
		// TODO: remove following
		allSolvers.add("test1");
		allSolvers.add("test2");
		allSolvers.add("test3");
		return allSolvers;
	}

	public static int getCoresForSolver(String solverId) {
		int cores = 0;
		for (Slave slave : slaves) {
			if (slave.getToolIds().contains(solverId)) {
				cores += slave.getCores();
			}
		}
		return cores;
	}

	public static Vector<Slave> getSlaves() {
		if (slaves == null) {
			slaves = new Vector<Slave>();
		}
		return slaves;
	}

	public static Vector<Slave> getSlavesForSolver(String solverId) {
		Vector<Slave> slavesWithSolver = new Vector<Slave>();

		for (Slave slave : slaves) {
			if (slave.getToolIds().contains(solverId)) {
				slavesWithSolver.add(slave);
			}
		}

		return slavesWithSolver;
	}

	public static AbstractTableModel getTableModel() {
		return tableModel;
	}

	private static void removeSlave(Slave slave) {
		slaves.remove(slave);
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	public static void setTableModel(AbstractTableModel tableModel) {
		Slave.tableModel = tableModel;
	}

	private MessageConsumer consumer_rcv;

	private int cores;

	private Destination destination_rcv;

	private Destination destination_snd;

	private String hostName;

	private MessageProducer producer_snd;

	private boolean running;

	private Map<String, TransmissionQbf> runningComputations = new HashMap<String, TransmissionQbf>();

	// private String user = ActiveMQConnection.DEFAULT_USER;
	// private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private Session session;

	/*
	 * private static void removeSlave(int slave) {
	 * removeSlave(slaves.get(slave)); }
	 */

	private Vector<String> toolIds;

	public void abortFormulaComputation(String tqbfId) {
		this.sendAbortMessage(tqbfId);
	}

	public void computeFormula(TransmissionQbf tqbf, String jobId) {
		this.sendFormulaMessage(tqbf, jobId);
		this.runningComputations.put(tqbf.getId(), tqbf);
	}

	public String[] getAssignedJobIds() {
		return new String[] { "test1", "test2" };
	}

	public int getCores() {
		return cores;
	}

	public String getHostName() {
		return hostName;
	}

	public Map<String, TransmissionQbf> getRunningComputations() {
		return runningComputations;
	}

	public Vector<String> getToolIds() {
		return toolIds;
	}

	private void handleAbortConfirmMessage(AbortConfirmMessage m) {
	}

	private void handleInformationMessage(InformationMessage m) {
		this.setHostName(m.getHostName());
		this.setCores(m.getCores());
		this.setToolIds(m.getToolIds());
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	private void handleResultMessage(ResultMessage m) {
		// Qbf.merge
	}

	private void handleShutdownMessage(ShutdownMessage m) {
		stop();
		// TODO notify someone about unfinished computation
		Slave.removeSlave(this);
	}

	public void kill(String reason) {
		this.sendKillMessage(reason);
		this.stop();
		Slave.removeSlave(this);
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
		if (t instanceof AbortConfirmMessage) {
			handleAbortConfirmMessage((AbortConfirmMessage) m);
		} else if (t instanceof InformationMessage) {
			handleInformationMessage((InformationMessage) m);
		} else if (t instanceof ResultMessage) {
			handleResultMessage((ResultMessage) m);
		} else if (t instanceof ShutdownMessage) {
			handleShutdownMessage((ShutdownMessage) m);
		} else {
			logger.error("Received message object of unknown type.");
		}
	}

	public void sendAbortMessage(String tqbfId) {
		logger.info("Sending AbortMessage to Slave " + this.getHostName());
		AbortMessage msg = new AbortMessage();
		msg.setQbfId(tqbfId);
		sendObject(msg);
		logger.info("AbortMessage sent");
	}

	public void sendFormulaMessage(TransmissionQbf tqbf, String jobId) {
		logger.info("Sending FormulaMessage to Slave " + this.getHostName());
		FormulaMessage msg = new FormulaMessage();
		msg.setFormula(tqbf);
		msg.setJobId(jobId);
		sendObject(msg);
		logger.info("FormulaMessage sent");
	}

	public void sendInformationRequestMessage() {
		logger.info("Sending InformationRequestMessage to Slave " + this.getHostName());
		InformationRequestMessage msg = new InformationRequestMessage();
		sendObject(msg);
		logger.info("InformationRequestMessage sent");
	}

	public void sendKillMessage(String reason) {
		logger.info("Sending KillMessage to Slave " + this.getHostName());
		KillMessage msg = new KillMessage();
		msg.setReason(reason);
		sendObject(msg);
		logger.info("KillMessage sent");
	}

	private void sendObject(Serializable o) {
		try {
			producer_snd.send(session.createObjectMessage(o));
		} catch (JMSException e) {
			logger.error("Error while sending Objectmessage...\n" + e.getStackTrace());
		}
	}

	public void setCores(int cores) {
		this.cores = cores;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setToolIds(Vector<String> toolIds) {
		this.toolIds = toolIds;
	}

	private void start() throws JMSException {
		logger.info("Starting Slavehandlerthread...");
		session = MasterDaemon.createSession();
		destination_snd = session.createQueue("TO." + hostName);
		destination_rcv = session.createQueue("FROM." + hostName);
		producer_snd = session.createProducer(destination_snd);
		producer_snd.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		consumer_rcv = session.createConsumer(destination_rcv);

		this.running = true;
		Message msg = null;
		while (running) {
			try {
				msg = consumer_rcv.receive(1000);
			} catch (JMSException e) {
				logger.error("Error while consuming Slavemessage...\n" + e.getStackTrace());
			}
			if (msg != null) {
				onMessage(msg);
			}
		}
		logger.info("Slavehandlerthread stopped");
	}

	public void stop() {
		logger.info("Stopping Slavethread...");
		//Stopping the loop
		this.running = false;
		try {
			Thread.sleep(1000);
			this.session.close();
		} catch (Exception e) {
			logger.error("Error while stopping Slavehandler...\n" + e.getStackTrace());
		}
	}
}
