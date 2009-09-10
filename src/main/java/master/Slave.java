package main.java.master;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
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
import main.java.messages.SlaveShutdownMessage;
import main.java.slave.SlaveDaemon;
import main.java.slave.solver.QProSolver;
import main.java.slave.solver.Solver;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Slave {
	private int cores;
	private Vector<String> toolIds;
	private String hostAddress;
	
	private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private Connection connection;
    private Session session;
    private Destination destination_rcv;
    private Destination destination_snd;
    private MessageProducer producer_snd;
    private MessageConsumer consumer_rcv;
    private boolean running;
    private static Vector<Slave> slaves = new Vector<Slave>();
	private static AbstractTableModel tableModel;
	
    public static AbstractTableModel getTableModel() {
		return tableModel;
	}

	public static void setTableModel(AbstractTableModel tableModel) {
		Slave.tableModel = tableModel;
	}

	public static Set<String> getAllAvaliableSolverIds() {
    	Set<String> allSolvers = new HashSet<String>();
    	for(Slave slave : slaves) {
    		for(String id : slave.getToolIds()) {
    			allSolvers.add(id);
    		}
    	}
    	return allSolvers;
    }
    
	public static void removeSlave(Slave slave) {
		if(tableModel != null) {
			tableModel.fireTableDataChanged();
		}
    	slaves.remove(slave);
	}
	
    public static void addSlave(Slave slave) {
		if(tableModel != null) {
			tableModel.fireTableDataChanged();
		}
    	slaves.add(slave);
	}
	
	public static Vector<Slave> getSlaves() {
		if(slaves == null) {
			slaves = new Vector<Slave>();
		} 
		return slaves;
	}
    
	public String getHostAddress() {
		return hostAddress;
	}
	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
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
	    
    public void connect(String url) throws JMSException, UnknownHostException {
    	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, 1);
        destination_snd = session.createQueue("TO." + hostAddress);
        destination_rcv = session.createQueue("FROM." + hostAddress);
        producer_snd = session.createProducer(destination_snd);
        producer_snd.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        consumer_rcv = session.createConsumer(destination_rcv);
    }
    
    public void disconnect(){
    	this.running = false;
    	try {
			Thread.sleep(1000);
	    	this.session.close();
	    	this.connection.close();
    	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void startConsuming() {
    	this.running = true;
    	Message msg = null;
    	while(running) {
    		try {
				msg = consumer_rcv.receive(1000);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msg != null) {
				onMessage(msg);
			}
    	}
    }
    
    public void stopConsuming() {
    	this.running = false;
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
		if(t instanceof AbortConfirmMessage) {
			handleAbortConfirmMessage((AbortConfirmMessage) m);
		} else if(t instanceof InformationMessage) {	
			handleInformationMessage((InformationMessage) m);
		} else if(t instanceof ResultMessage) {
			handleResultMessage((ResultMessage) m);
		} else if(t instanceof ShutdownMessage) {
			handleShutdownMessage((ShutdownMessage) m);
		} else {
			System.err.println("Message object of unknown type.");
		}
	}
	
	private void handleShutdownMessage(ShutdownMessage m) {
		disconnect();
		// TODO notify someone about unfinished computation
		slaves.remove(this);
	}
	
	private void handleResultMessage(ResultMessage m) {
		
	}
	
	private void handleInformationMessage(InformationMessage m) {
		// TODO Auto-generated method stub
		
	}
	
	private void handleAbortConfirmMessage(AbortConfirmMessage m) {
		// TODO Auto-generated method stub
		
	}
	
	private void sendAbortMessage() {
		AbortMessage msg = new AbortMessage();
	}
	
	private void sendFormulaMessage(String jobId, TransmissionQbf tqbf) {
		FormulaMessage msg = new FormulaMessage();
		msg.setFormula(tqbf);
	}
	
	private void sendInformationRequestMessage() {
		InformationRequestMessage msg = new InformationRequestMessage();
		sendObject(msg);
	}
	
	private void sendKillMessage(String reason) {
		KillMessage msg = new KillMessage();
		msg.setReason(reason);
		sendObject(msg);
	}
}
