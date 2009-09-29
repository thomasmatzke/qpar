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

public class Slave implements MessageListener {
	private int cores;
	private Vector<String> toolIds;
	private String hostName;

	//private String user = ActiveMQConnection.DEFAULT_USER;
	//private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private Session session;
	private Destination destination_rcv;
	private Destination destination_snd;
	private MessageProducer producer_snd;
	private MessageConsumer consumer_rcv;
	private boolean running;
	private static Vector<Slave> slaves = new Vector<Slave>();
	private static AbstractTableModel tableModel;
	private Map<String, TransmissionQbf> runningComputations = new HashMap<String, TransmissionQbf>();

	public Map<String, TransmissionQbf> getRunningComputations() {
		return runningComputations;
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

	public static int getCoresForSolver(String solverId) {
		int cores = 0;
		for (Slave slave : slaves) {
			if (slave.getToolIds().contains(solverId)) {
				cores += slave.getCores();
			}
		}
		return cores;
	}

	public static Slave create(String hostName) throws JMSException {
		Slave instance = new Slave();
		instance.start();
		Slave.addSlave(instance);
		return instance;
	}

	public static AbstractTableModel getTableModel() {
		return tableModel;
	}

	public static void setTableModel(AbstractTableModel tableModel) {
		Slave.tableModel = tableModel;
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

	private static void removeSlave(Slave slave) {
		slaves.remove(slave);
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	/*private static void removeSlave(int slave) {
		removeSlave(slaves.get(slave));
	}*/

	private static void addSlave(Slave slave) {
		slaves.add(slave);
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	public static Vector<Slave> getSlaves() {
		if (slaves == null) {
			slaves = new Vector<Slave>();
		}
		return slaves;
	}

	public void abortFormulaComputation(String tqbfId) {
		this.sendAbortMessage(tqbfId);
	}

	public void kill(String reason) {
		this.sendKillMessage(reason);
		this.stop();
		Slave.removeSlave(this);
	}

	public void computeFormula(TransmissionQbf tqbf, String jobId) {
		this.sendFormulaMessage(tqbf, jobId);
		this.runningComputations.put(tqbf.getId(), tqbf);
	}

	public String[] getAssignedJobIds() {
		return new String[] { "test1", "test2" };
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getCores() {
		return cores;
	}

	public void setCores(int cores) {
		this.cores = cores;
	}

	public Vector<String> getToolIds() {
		return toolIds;
	}

	public void setToolIds(Vector<String> toolIds) {
		this.toolIds = toolIds;
	}

	private void start() throws JMSException {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (msg != null) {
				onMessage(msg);
			}
		}
	}

	public void stop() {
		this.running = false;
		try {
			Thread.sleep(1000);
			this.session.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendObject(Serializable o) {
		try {
			producer_snd.send(session.createObjectMessage(o));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onMessage(Message m) {
		if (!(m instanceof ObjectMessage)) {
			System.err.println("Received Non-ObjectMessage");
			return;
		}
		Object t = null;
		try {
			t = ((ObjectMessage) m).getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			System.err.println("Message object of unknown type.");
		}
	}

	private void handleShutdownMessage(ShutdownMessage m) {
		stop();
		// TODO notify someone about unfinished computation
		Slave.removeSlave(this);
	}

	private void handleResultMessage(ResultMessage m) {
		// Qbf.merge
	}

	private void handleInformationMessage(InformationMessage m) {
		this.setHostName(m.getHostName());
		this.setCores(m.getCores());
		this.setToolIds(m.getToolIds());
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	private void handleAbortConfirmMessage(AbortConfirmMessage m) {
	}

	public void sendAbortMessage(String tqbfId) {
		AbortMessage msg = new AbortMessage();
		msg.setQbfId(tqbfId);
		sendObject(msg);
	}

	public void sendFormulaMessage(TransmissionQbf tqbf, String jobId) {
		FormulaMessage msg = new FormulaMessage();
		msg.setFormula(tqbf);
		msg.setJobId(jobId);
		sendObject(msg);
	}

	public void sendInformationRequestMessage() {
		InformationRequestMessage msg = new InformationRequestMessage();
		sendObject(msg);
	}

	public void sendKillMessage(String reason) {
		KillMessage msg = new KillMessage();
		msg.setReason(reason);
		sendObject(msg);
	}
}
