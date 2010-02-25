package main.java.master;

import java.io.Serializable;
import java.util.Date;
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
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import main.java.Util;
import main.java.logic.Qbf;
import main.java.logic.TransmissionQbf;
import main.java.messages.ErrorMessage;
import main.java.messages.FormulaAbortedMessage;
import main.java.messages.AbortMessage;
import main.java.messages.FormulaMessage;
import main.java.messages.InformationMessage;
import main.java.messages.InformationRequestMessage;
import main.java.messages.KillMessage;
import main.java.messages.Ping;
import main.java.messages.Pong;
import main.java.messages.ResultMessage;
import main.java.messages.ShutdownMessage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Slave implements MessageListener, Runnable {

	public static final int READY = 0;
	public static final int BUSY = 1;

	public int status;
	
	// The slave has that much time to answer to the ping
	public static final long KEEPALIVE_TIMEOUT = 10 * 1000; // In Millis

	static Logger logger = Logger.getLogger(MasterDaemon.class);
	{
		logger.setLevel(Level.INFO);
	}
	private static Map<String, Slave> slaves = new HashMap<String, Slave>();
	private static AbstractTableModel tableModel;
	private long lastPingMillis = 0;
	private long lastPongMillis = System.currentTimeMillis();

	private static void addSlave(Slave slave) {
		slaves.put(slave.hostName, slave);
		logger.debug("Adding Slave: " + slave);
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	public static Slave create(String hostName, int cores,
			Vector<String> solvers) {
		logger.info("Starting new Slaveinstance/thread...");
		Slave instance = new Slave();
		instance.cores = cores;
		instance.hostName = hostName;
		instance.setToolIds(solvers);
		instance.status = Slave.READY;
		new Thread(instance).start();
		Slave.addSlave(instance);
		return instance;
	}

	public static Set<String> getAllAvaliableSolverIds() {
		Set<String> allSolvers = new HashSet<String>();
		for (Slave slave : slaves.values()) {
			for (String id : slave.getToolIds()) {
				allSolvers.add(id);
			}
		}
		return allSolvers;
	}

	public static int getCoresForSolver(String solverId) {
		int cores = 0;
		for (Slave slave : slaves.values()) {
			if (slave.getToolIds().contains(solverId) && slave.status == Slave.READY) {
				cores += slave.getCores();
			}
		}
		return cores;
	}

	public static Map<String, Slave> getSlaves() {
		if (slaves == null) {
			slaves = new HashMap<String,Slave>();
		}
		return slaves;
	}

	public static Vector<Slave> getSlavesWithSolver(String solverId) {
		Vector<Slave> slavesWithSolver = new Vector<Slave>();

		for (Slave slave : slaves.values()) {
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
		slaves.remove(slave.getHostName());
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

	// Maps tqbf ids to Jobs
	private Map<String, Job> runningComputations = new HashMap<String, Job>();

	private Session session;

	private Vector<String> toolIds;

	public void abortFormulaComputation(String tqbfId) {
		this.sendAbortMessage(tqbfId);
	}

	public void computeFormula(TransmissionQbf tqbf, Job job) {
		this.sendFormulaMessage(tqbf, job.getSolver());
		this.runningComputations.put(tqbf.getId(), job);
	}

	public int getCores() {
		return cores;
	}

	public String getHostName() {
		return hostName;
	}

	public Map<String, Job> getRunningComputations() {
		return runningComputations;
	}

	public Vector<String> getToolIds() {
		return toolIds;
	}

	private void handleFormulaAbortedMessage(FormulaAbortedMessage m) {
		logger.info("Receiving FormulaAbortedMessage from " + this.getHostName());
		this.runningComputations.remove(m.getTqbfId());
		logger.info("Removed tqbf(" + m.getTqbfId()
				+ ") from running computations.");
	}

	private void handleErrorMessage(ErrorMessage t) {
		logger.error("Receiving ErrorMessage from " + this.getHostName() + ":" + t.getMessage());
		Job job = this.runningComputations.get(t.getTQbfId());
		job.abort();
	}
	
	private void handleInformationMessage(InformationMessage m) {
		logger.info("Receiving InformationMessage from " + this.getHostName());
		this.setHostName(m.getHostName());
		this.setCores(m.getCores());
		this.setToolIds(m.getToolIds());
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
		logger.info("Slave information updated.");
	}

	private void handleResultMessage(ResultMessage m) {
		logger.info("Receiving ResultMessage from " + this.getHostName());
		Job job = this.runningComputations.get(m.getTqbfId());
		Qbf formula = job.getFormula();
		boolean solved = formula.mergeQbf(m.getTqbfId(), m.getResult());
		this.runningComputations.remove(m.getTqbfId());
		logger.info("Result of tqbf(" + m.getTqbfId()
				+ ") merged into Qbf of Job " + job.getId());
		if (solved) {
			job.fireJobCompleted(formula.getResult());
		}
	}

	private void handleShutdownMessage(ShutdownMessage m) {
		logger.info("ShutdownMessage received. Removing slave "
				+ this.getHostName());
		stop();
		handleDeath();
		Slave.removeSlave(this);
		logger.info("Slave " + this.getHostName() + " removed");
	}

	
	
	public void kill(String reason) {
		logger.info("Killing Slave " + this.getHostName() + " ...");
		this.sendKillMessage(reason);
		logger.info("Kill-Message sent to " + this.getHostName());
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
			logger.error("Error while retrieving Object from Message... \n"
					+ e);
		}
		logger.debug("Received message of type " + t.getClass().toString());
		if (t instanceof FormulaAbortedMessage) {
			handleFormulaAbortedMessage((FormulaAbortedMessage) t);
		} else if (t instanceof InformationMessage) {
			handleInformationMessage((InformationMessage) t);
		} else if (t instanceof ResultMessage) {
			handleResultMessage((ResultMessage) t);
		} else if (t instanceof ShutdownMessage) {
			handleShutdownMessage((ShutdownMessage) t);
		} else if (t instanceof Pong) {
			this.lastPongMillis = System.currentTimeMillis();
		} else if (t instanceof ErrorMessage) {
			handleErrorMessage((ErrorMessage) t);
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

	public void sendFormulaMessage(TransmissionQbf tqbf, String solver) {
		logger.info("Sending FormulaMessage to Slave " + this.getHostName());
		FormulaMessage msg = new FormulaMessage();
		msg.setFormula(tqbf);
		msg.setSolver(solver);
		sendObject(msg);
		logger.info("FormulaMessage sent");
	}

	public void sendInformationRequestMessage() {
		logger.info("Sending InformationRequestMessage to Slave "
				+ this.getHostName());
		InformationRequestMessage msg = new InformationRequestMessage();
		sendObject(msg);
	}

	public void sendKillMessage(String reason) {
		logger.info("Sending KillMessage to Slave " + this.getHostName());
		KillMessage msg = new KillMessage();
		msg.setReason(reason);
		sendObject(msg);
	}

	public void sendPing() {
		sendObject(new Ping());
		this.lastPingMillis = System.currentTimeMillis();
	}

	private void sendObject(Serializable o) {
		try {
			logger.debug("Sending Object of Class : " + o.getClass() + " to "
					+ producer_snd.getDestination());
			producer_snd.send(session.createObjectMessage(o));
		} catch (JMSException e) {
			logger.error("Error while sending Objectmessage...\n"
					+ e);
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

	public void stop() {
		logger.info("Stopping Slavethread...");
		// Stopping the loop
		this.running = false;
		try {
			Thread.sleep(1000);
			this.session.close();
		} catch (Exception e) {
			logger.error("Error while stopping Slavehandler...\n"
					+ e);
		}
	}

	@Override
	public void run() {
		logger.info("Starting Slavehandlerthread...");
		try {
			session = MasterDaemon.createSession();
			destination_snd = session.createQueue("TO." + hostName);
			destination_rcv = session.createQueue("FROM." + hostName);
			producer_snd = session.createProducer(destination_snd);
			producer_snd.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			consumer_rcv = session.createConsumer(destination_rcv);
		} catch (JMSException e) {
			logger.error("Error while initializing Queues...\n" + e);
			System.exit(-1);
		}

		this.running = true;
		Message msg = null;

		long currentMillis;
		this.sendPing();

		while (running) {
			try {
				msg = consumer_rcv.receive(1000);
			} catch (JMSException e) {
				logger.error("Error while consuming Slavemessage...\n"
						+ e);
			}
			if (msg != null) {
				onMessage(msg);
			}
			currentMillis = System.currentTimeMillis();

			if ((this.lastPingMillis + KEEPALIVE_TIMEOUT) < currentMillis) {
				if (this.lastPingMillis <= this.lastPongMillis
						&& this.lastPongMillis < currentMillis) {
					this.sendPing();
				} else {
					logger.error("Slave " + this.getHostName()
							+ " timed out. Removing from pool...");
					stop();
					handleDeath();
					Slave.removeSlave(this);
				}
			}

		}
		logger.info("Slavehandlerthread stopped");

	}

	private void handleDeath() {
		if(this.runningComputations.size() < 1) return;
		for(Job job : Job.getJobs().values()) {
			if(job.getFormulaDesignations().values().contains(this)) {
				job.abort();
				job.setStatus(Job.ERROR);
				if (Job.getTableModel() != null) {
					SwingUtilities.invokeLater(new Runnable(){
							public void run() {
								Job.getTableModel().fireTableDataChanged();
							}
					});
					
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "Slave -- Hostname: " + this.getHostName() + ", Solvers: "
				+ (this.toolIds != null ? this.toolIds.toString() : "")
				+ ", Cores: " + this.cores;
	}

	public String[] getCurrentJobs() {
		Vector<String> jobs = new Vector<String>();
		for(Job job : this.getRunningComputations().values()) {
			jobs.add(job.getId());
		}
		String[] jobsArr = new String[jobs.size()];
		jobs.toArray(jobsArr);
		
		Set<String> set = new HashSet<String>(jobs);
		String[] uniqJobs = (set.toArray(new String[set.size()]));
		return uniqJobs;
	}


}
