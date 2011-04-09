package main.java.master;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import main.java.common.rmi.InterpretationData;
import main.java.common.rmi.RemoteObserver;
import main.java.common.rmi.Result;
import main.java.common.rmi.SlaveRemote;
import main.java.common.rmi.TQbfRemote;
import main.java.common.rmi.WrappedObserver;
import main.java.master.logic.parser.SimpleNode;
import main.java.slave.solver.Solver;
import main.java.slave.tree.Reducer;

import org.apache.log4j.Logger;

public class TQbf extends Observable implements TQbfRemote {
	private static final long serialVersionUID = -6627723521432123349L;
	static Logger logger = Logger.getLogger(TQbf.class);
		
	public enum State { NEW, COMPUTING, TERMINATED, DONTSTART, ABORTED, TIMEOUT, ERROR, MERGED }
	public byte[] serializedFormula = null;
	
	private String id;
	private long timeout;
	private String jobId;
	private ArrayList<Integer> trueVars = new ArrayList<Integer>();
	private ArrayList<Integer> falseVars = new ArrayList<Integer>();
	
	private ArrayDeque<SimpleNode> truthAssignedNodes = null;

	private String solverId = null;

	private Result result = null;
	
	private Map<State, Date> history = new HashMap<State, Date>();
	private State state = State.NEW;
	
	private SlaveRemote slave;
	
	public TQbf(String tqbfId, String jobId, String solverId, ArrayList<Integer> trueVars, ArrayList<Integer> falseVars, long timeout, byte[] serializedFormula) throws RemoteException {
		super();
		this.id = tqbfId;
		this.jobId = jobId;
		this.solverId = solverId;
		this.trueVars = trueVars;
		this.falseVars = falseVars;
		this.timeout = timeout;
		this.serializedFormula = serializedFormula;
		
		UnicastRemoteObject.exportObject(this, 0);
	}
	
	synchronized public void compute(SlaveRemote slave) {
		logger.info("Computing formula " + this.getId() + "...");
		this.setSlave(slave);
		try {
			slave.computeFormula(this);
		} catch (RemoteException e) {
			logger.error("", e);
			this.setState(State.ERROR);
			return;
		}
		this.setState(TQbf.State.COMPUTING);		
	}
	
	public long getComputationTime() {
		if(this.isComputing()) {
			return new Date().getTime() - history.get(State.COMPUTING).getTime();
		} else if(this.isTerminated() || this.isMerged()) {
			return history.get(State.TERMINATED).getTime() - history.get(State.COMPUTING).getTime();
		} else {
			throw new IllegalStateException("TQbf was not computing or terminated/merged");
		}		
	}
	
	synchronized public void setMerged() {
		if(!this.getState().equals(State.TERMINATED))
			throw new IllegalStateException("Cant merge a non TERMINATED tqbf. State was: " + this.getState());
		this.setState(State.MERGED);
	}
	
	synchronized public void abort() {
		switch(this.getState()) {
			case NEW:
				this.setState(State.DONTSTART);
				break;
			case COMPUTING:
				try {
					this.slave.abortFormula(this.getId());
				} catch (RemoteException e) {
					logger.error("", e);
				}
				this.setState(State.ABORTED);
				break;
			case ABORTED:
			case TERMINATED:
			case TIMEOUT:
			case MERGED:
				break;
			case DONTSTART:
			default:
				assert(false);
		}				
	}
	
	synchronized public void timeout() {
		if(!this.isComputing())
			throw new IllegalStateException("Tqbf must be computing to change to state TIMEOUT. State was: " + this.getState());
		this.setState(State.TIMEOUT);
	}
	
	synchronized public void handleResult(Result r) throws RemoteException{
		if(this.isComputing())
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
		if(this.isAborted() || this.isMerged() || this.isError() || this.isTimeout())
			throw new IllegalStateException();
		logger.info("Tqbf " + this.getId() + " to change state from " + this.state + " to " + state);
		this.state = state;
		history.put(state, new Date());
		setChanged();
        notifyObservers();
	}
		
	synchronized public Result getResult() {
		if(this.result == null)
			throw new IllegalStateException("No result available. Tqbf State: " + this.getState());
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
	
	public boolean isError() {
		return this.state == State.ERROR ? true : false;
	}
	
	public boolean isTimeout() {
		return this.state == State.TIMEOUT ? true : false;
	}
	
	public boolean isMerged() {
		return this.state == State.MERGED ? true : false;
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
