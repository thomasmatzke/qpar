package main.java.master;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;

import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.SimpleNode.NodeType;
import main.java.rmi.InterpretationData;
import main.java.rmi.RemoteObserver;
import main.java.rmi.Result;
import main.java.rmi.SlaveRemote;
import main.java.rmi.TQbfRemote;
import main.java.rmi.WrappedObserver;
import main.java.slave.Slave;
import main.java.slave.solver.ResultHandler;
import main.java.slave.solver.Solver;
import main.java.slave.solver.SolverFactory;
import main.java.tree.OrphanVisitor;
import main.java.tree.Reducer;

import org.apache.log4j.Logger;

// A TransmissionQbf contains a QBF as a tree as well as vectors and lists of
// all vars/exist-quantified vars/all-quantified vars/vars to assign true and
// vars to assign false. They are created in the Qbf class and sent to the
// slaves to be solved there.
public class TQbf extends Observable implements ResultHandler, TQbfRemote {
	private static final long serialVersionUID = -6627723521432123349L;
	static Logger logger = Logger.getLogger(TQbf.class);
	
	public TQbf() throws RemoteException {
		super();
		UnicastRemoteObject.exportObject(this, 0);
	}
	
	public enum State { NEW, COMPUTING, TERMINATED, DONTSTART, ABORTED, TIMEOUT }
		
	private transient SimpleNode root = null;
	public byte[] serializedFormula = null;
	
	private String id;
	private long timeout;
	private String jobId;
	private Vector<Integer> eVars = new Vector<Integer>();
	private Vector<Integer> aVars = new Vector<Integer>();
	private Vector<Integer> vars = new Vector<Integer>();
	private ArrayList<Integer> trueVars = new ArrayList<Integer>();
	private ArrayList<Integer> falseVars = new ArrayList<Integer>();
	
	private ArrayDeque<SimpleNode> truthAssignedNodes = null;

	private String solverId = null;

	private Result result = null;
	
	private Map<State, Date> history = new HashMap<State, Date>();
	private State state = State.NEW;
	
	private SlaveRemote slave;
	private Solver solver;
	
	synchronized public void compute(SlaveRemote slave) {
		this.setSlave(slave);
		logger.info("Computing formula " + this.getId() + "...");
		Solver solver = SolverFactory.getSolver(this.getSolverId(), this, this);
		this.solver = solver;
		Slave.globalThreadPool.execute(solver);
		this.setState(TQbf.State.COMPUTING);
	}
	
	public long getComputationTime() {
		if(this.isComputing()) {
			return new Date().getTime() - history.get(State.COMPUTING).getTime();
		} else if(this.isTerminated()) {
			return history.get(State.TERMINATED).getTime() - history.get(State.COMPUTING).getTime();
		} else {
			throw new IllegalStateException("TQbf was not computing or terminated");
		}		
	}
	
	synchronized public void abort() {
		switch(this.getState()) {
			case NEW:
				this.setState(State.DONTSTART);
				break;
			case COMPUTING:
				this.solver.kill();
				this.setState(State.ABORTED);
				break;
			case ABORTED:
			case TERMINATED:
			case TIMEOUT:
				break;
			case DONTSTART:
			default:
				assert(false);
		}				
	}
	
	synchronized public void timeout() {
		if(!this.getState().equals(State.COMPUTING))
			throw new IllegalStateException("Tqbf must be computing to change to state TIMEOUT");
		this.setState(State.TIMEOUT);
	}
	
	@Override
	synchronized public void handleResult(Result r) {
		this.setResult(r);		
	}
	
	/**
	 * Blocks until computation is terminated
	 */
	public void waitFor() {
		synchronized(this) {
			while(result == null) {
				try { wait(); } catch (InterruptedException e) {}
			}
		}
	}
		
	synchronized public void setResult(Result r) {
		synchronized(this) {
			this.result = r;
			notifyAll();
		}
		this.setState(State.TERMINATED);
	}
	
	private void setState(State state) {
		logger.info("Tqbf " + this.getId() + " to change state from " + this.state + " to " + state);
		this.state = state;
		history.put(state, new Date());
		setChanged();
        notifyObservers();
	}
		
	synchronized public Result getResult() {
		return result;
	}
	
	synchronized public State getState() {
		return state;
	}
	
	public void reduceFast() {
		ArrayDeque<SimpleNode> reducableNodes = new ArrayDeque<SimpleNode>();

		// The parents of the assignednodes are reducable, so lets get them
		for (SimpleNode n : truthAssignedNodes) {
			if (n.jjtGetParent() != null)
				reducableNodes.add((SimpleNode) n.jjtGetParent());
		}

		Reducer r = new Reducer(reducableNodes);
		r.reduce();
		// this.root.dump(this.getId() + "  ");
	}

	/**
	 * Print the tree
	 * 
	 * @param s
	 *            optional string to prefix the tree
	 */
	public void dump(String s) {
		this.getRootNode().dump(s);
	}

	/**
	 * getter for the trueVars ArrayList
	 * 
	 * @return the ArrayList of true-assigned vars
	 */
	public ArrayList<Integer> getTrueVars() {
		return trueVars;
	}

	/**
	 * getter for the falseVars ArrayList
	 * 
	 * @return the ArrayList of false-assigned vars
	 */
	public ArrayList<Integer> getFalseVars() {
		return falseVars;
	}

	public Integer getMaxVar() {
		ArrayList<Integer> vars = new ArrayList<Integer>(this.getAVars());
		vars.addAll(this.getEVars());
		return Collections.max(vars);
	}

	/**
	 * debug method that logs the content of a transmissionQbf
	 */
	public void checkQbf() {
		logger.debug("checkQBF id: " + this.getId());
		// logger.debug("checkQBF root node: " + root.getClass().getName() +
		// " with " +
		// root.jjtGetNumChildren() + " children (should be 1. first one: " +
		// root.jjtGetChild(0).getClass().getName() + ")");
		logger.debug("checkQBF vars: " + this.vars);
		logger.debug("checkQBF eVars: " + this.eVars);
		logger.debug("checkQBF aVars: " + this.aVars);
		logger.debug("checkQBF trueVars: " + this.trueVars);
		logger.debug("checkQBF falseVars: " + this.falseVars);
	}

	/**
	 * getter for the eVars Vector
	 * 
	 * @return the Vector of exist-quantified vars
	 */
	public Vector<Integer> getEVars() {
		return this.eVars;
	}

	/**
	 * getter for the aVars Vector
	 * 
	 * @return the Vector of all-quantified vars
	 */
	public Vector<Integer> getAVars() {
		return this.aVars;
	}

	/**
	 * getter for the vars Vector
	 * 
	 * @return the Vector of all variables that appear in a formula
	 */
	public Vector<Integer> getVars() {
		return this.vars;
	}

	/**
	 * setter for the exist-quantified vars
	 * 
	 * @param v
	 *            vector of exist-quantified vars
	 */
	public void setEVars(Vector<Integer> v) {
		this.eVars = v;
	}

	/**
	 * setter for the all-quantified vars
	 * 
	 * @param v
	 *            vector of all-quantified vars
	 */
	public void setAVars(Vector<Integer> v) {
		this.aVars = v;
	}

	/**
	 * setter for the vars
	 * 
	 * @param v
	 *            vector of all vars
	 */
	public void setVars(Vector<Integer> v) {
		this.vars = v;
	}

	/**
	 * assigns the transmissionQbf-specific truth values to the tree.
	 */
//	public void assignTruthValues() {
//		int i;
//		ArrayDeque<SimpleNode> assigned = new ArrayDeque<SimpleNode>();
//		for (i = 0; i < this.trueVars.size(); i++) {
//			assigned.addAll(this.getRootNode().assignTruthValue(this.trueVars.get(i), true));
//		}
//		for (i = 0; i < this.falseVars.size(); i++) {
//			assigned.addAll(this.getRootNode().assignTruthValue(this.falseVars.get(i), false));
//		}
//
//		this.truthAssignedNodes = assigned;
//	}

	/**
	 * calls the reduce() method of the tree as long as there's something to
	 * reduce
	 */
//	public void reduceTree() {
//		boolean continueReducing = true;
//
//		while (continueReducing) {
//			continueReducing = this.getRootNode().reduce();
//		}
//	}

	/**
	 * calls the traverse() method of the tree.
	 * 
	 * @return The formula (without header and footer) in QPro format.
	 */
//	public String traverseTree() {
//		return this.getRootNode().jjtGetChild(0).traverse();
//	}

	/**
	 * checks if the first node after the input node has a truth value assigned
	 * 
	 * @return true if the first node after input has a truth value, false
	 *         otherwise
	 */
	public boolean rootIsTruthNode() {

		SimpleNode start = (SimpleNode) this.getRootNode().jjtGetChild(0);
		return start.isTruthNode();
		//
		// while ((start.getNodeType() == NodeType.FORALL) ||
		// (start.getNodeType() == NodeType.EXISTS)) {
		// start = (SimpleNode)start.jjtGetChild(0);
		// }
		//
		// if (start.getTruthValue().equals(""))
		// return false;
		// return true;
	}

	/**
	 * returns the truth value of the formulas root node
	 * 
	 * @return a truth value
	 */
	public boolean rootGetTruthValue() {
		if (this.getRootNode().jjtGetChild(0).getNodeType() == NodeType.TRUE)
			return true;
		return false;
	}

	/**
	 * returns a vector of vars from aVars & eVars that don't appear in the tree
	 * 
	 * @return a vector of orphaned quantified vars
	 */
	// public Vector<Integer> getOrphanedVars() {
	// Vector<Integer> orphanedVars = new Vector<Integer>();
	// Vector<Integer> quantifiedVars = new Vector<Integer>();
	//
	// quantifiedVars.addAll(aVars);
	// quantifiedVars.addAll(eVars);
	//
	// for (int i = 0; i < quantifiedVars.size(); i++) {
	// if (!root.findVar(quantifiedVars.get(i))) {
	// orphanedVars.add(quantifiedVars.get(i));
	// }
	// }
	// return orphanedVars;
	// }

	public void eliminateOrphanedVars() {
		ArrayList<Integer> quantifiedVars = new ArrayList<Integer>(aVars);
		quantifiedVars.addAll(eVars);

		OrphanVisitor v = new OrphanVisitor(quantifiedVars);
		v.visit(this.getRootNode());
		ArrayList<Integer> orphans = v.getOrpahns();
		// logger.info("Eliminating vars(" + orphans.size() + "): " + orphans);
		aVars.removeAll(orphans);
		eVars.removeAll(orphans);
	}

	public SimpleNode getRootNode() {
		if(this.root == null) {
			if(this.serializedFormula == null) {
				// This is weird...
				logger.error("TransmissionQbf didnt contain a formula tree or a serialized formula-tree");
				assert(false);
				return null;
			} else {
				// This means the root was nullified by transient while serialization
				// deserialize and write to root-variable
				try {
					ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(this.serializedFormula));
					this.root = (SimpleNode) in.readObject();
					in.close();
					return root;
				} catch (Exception e) {
					// this sucks...
					logger.error("Problem while deserializing formula", e);
					return null;
				}
			}
		} else {
			return this.root;
		}
	}

	public void setRootNode(SimpleNode n) {
		this.root = n;
	}

	public TQbf deepClone() throws Exception {
		TQbf clonedObj = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.close();
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		clonedObj = (TQbf) ois.readObject();
		ois.close();
		return clonedObj;
	}

	@Override
	public void addObserver(RemoteObserver o) throws RemoteException {
//		logger.info("Adding observer");
		WrappedObserver mo = new WrappedObserver(o);
        addObserver(mo);
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setSolverId(String solverId) {
		this.solverId = solverId;
	}

	public String getSolverId() {
		return solverId;
	}

	public boolean isComputing() {
		return this.state == State.COMPUTING ? true : false;
	}
	
	public boolean isNew() {
		return this.state == State.NEW ? true : false;
	}
	
	public boolean isTerminated() {
		return this.state == State.TERMINATED ? true : false;
	}
	
	public boolean isDontstart() {
		return this.state == State.DONTSTART ? true : false;
	}
	
	public boolean isAborted() {
		return this.state == State.ABORTED ? true : false;
	}

	public void setSlave(SlaveRemote slave) {
		this.slave = slave;
	}

	public SlaveRemote getSlave() {
		return slave;
	}

	@Override
	public InterpretationData getWorkUnit() throws RemoteException {
		InterpretationData id = new InterpretationData(this.serializedFormula, this.trueVars, this.falseVars);
		return id;
	}
}
