/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package qpar.master;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;

import qpar.common.rmi.InterpretationData;
import qpar.common.rmi.SlaveRemote;
import qpar.common.rmi.TQbfRemote;

public class TQbf extends Observable implements TQbfRemote {
	public enum State { ABORTED, COMPUTING, DONTSTART, ERROR, MERGED, NEW, TERMINATED, TIMEOUT }
	static Logger logger = Logger.getLogger(TQbf.class);
		
	private static final long serialVersionUID = -6627723521432123349L;
	private ArrayList<Integer> falseVars = new ArrayList<Integer>();
	private ArrayList<Integer> trueVars = new ArrayList<Integer>();
	
	private Map<State, Date> history = new HashMap<State, Date>();
	private String id;
	private Job job;
	
	private SlaveRemote slave;

	private String solverId = null;

	private volatile State state = State.NEW;
	
	private long timeout;
	
	private long overheadMillis, solverMillis;
	
	private boolean result;
	
	public static HashMap<String, TQbf> tqbfs = new HashMap<String, TQbf>();
	
	public TQbf(String tqbfId, Job job, String solverId, ArrayList<Integer> trueVars, ArrayList<Integer> falseVars, long timeout, byte[] serializedFormula) throws RemoteException {
		super();
		this.id = tqbfId;
		this.job = job;
		this.solverId = solverId;
		this.trueVars = trueVars;
		this.falseVars = falseVars;
		this.timeout = timeout;
		
		UnicastRemoteObject.exportObject(this, 0);
		tqbfs.put(tqbfId, this);
	}
	
	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
	
	public boolean getResult() {
		return this.result;
	}

	public long getOverheadMillis() {
		return overheadMillis;
	}

	public void setOverheadMillis(long overheadMillis) {
		this.overheadMillis = overheadMillis;
	}

	public long getSolverMillis() {
		return solverMillis;
	}

	public void setSolverMillis(long solverMillis) {
		this.solverMillis = solverMillis;
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
				break;
			case ABORTED:
			case TERMINATED:
			case TIMEOUT:
			case MERGED:
			case ERROR:
			case DONTSTART:
				break;			
			default:
				logger.error("Unexpected TQbf state: " + this.getState());
				assert(false);
		}				
	}

	public long getComputationTime() {
		if(this.isComputing()) {
			return new Date().getTime() - history.get(State.COMPUTING).getTime();
		} else if(this.isTerminated() || this.isMerged()) {
			if(history.get(State.TERMINATED) == null)
				logger.info("terminated null. state: " + this.getState());
			if(history.get(State.COMPUTING) == null)
				logger.info("COMPUTING null state: " + this.getState());
			return history.get(State.TERMINATED).getTime() - history.get(State.COMPUTING).getTime();
		} else {
			throw new IllegalStateException("TQbf was not computing or terminated/merged");
		}		
	}
	
	/**
	 * getter for the falseVars ArrayList
	 * 
	 * @return the ArrayList of false-assigned vars
	 */
	public ArrayList<Integer> getFalseVars() {
		return falseVars;
	}
	
	public String getId() {
		return id;
	}
		
	public String getJobId() {
		return job.id;
	}
			
	public SlaveRemote getSlave() {
		return slave;
	}
	
	public String getSolverId() {
		return solverId;
	}
	
	synchronized public State getState() {
		return state;
	}

	public long getTimeout() {
		return timeout;
	}

	/**
	 * getter for the trueVars ArrayList
	 * 
	 * @return the ArrayList of true-assigned vars
	 */
	public ArrayList<Integer> getTrueVars() {
		return trueVars;
	}

	@Override
	public InterpretationData getWorkUnit() throws RemoteException {
		InterpretationData id = new InterpretationData(this.job.serializedFormula, this.trueVars, this.falseVars);
		return id;
	}

	synchronized public boolean isAborted() {
		return this.state == State.ABORTED ? true : false;
	}

	synchronized public boolean isComputing() {
		return this.state == State.COMPUTING ? true : false;
	}

	synchronized public boolean isDontstart() {
		return this.state == State.DONTSTART ? true : false;
	}

	synchronized public boolean isError() {
		return this.state == State.ERROR ? true : false;
	}

	synchronized public boolean isMerged() {
		return this.state == State.MERGED ? true : false;
	}

	synchronized public boolean isNew() {
		return this.state == State.NEW ? true : false;
	}

	synchronized public boolean isTerminated() {
		return this.state == State.TERMINATED ? true : false;
	}

	synchronized public boolean isTimeout() {
		return this.state == State.TIMEOUT ? true : false;
	}
	
	public void setId(String id) {
		this.id = id;
	}
		
	synchronized public void setMerged() {
		if(!this.isTerminated())
			throw new IllegalStateException("Cant merge a non TERMINATED tqbf. State was: " + this.getState());
		this.setState(State.MERGED);
	}
	
	public void setSlave(SlaveRemote slave) {
		this.slave = slave;
	}
	
	public void setSolverId(String solverId) {
		this.solverId = solverId;
	}
	
	synchronized public void setState(State state) {
		if(this.isAborted() || this.isMerged() || this.isError() || this.isTimeout()) {
			logger.error("Wanted to set state from " + this.getState() + " to " + state);
			throw new IllegalStateException();
		}
			
		logger.info("Tqbf " + this.getId() + " to change state from " + this.state + " to " + state);
		this.state = state;
		history.put(state, new Date());
		setChanged();
        
		Master.globalThreadPool.execute(
		new Runnable() {
			@Override
			public void run() {
				notifyObservers();
			}
		});	
        
        //notifyAll();
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	synchronized public void timeout() {
		if(!this.isComputing())
			throw new IllegalStateException("Tqbf must be computing to change to state TIMEOUT. State was: " + this.getState());
		this.setState(State.TIMEOUT);
	}
		
	@Override
	synchronized public void error() {
		logger.info("received error in state: " + this.getState());
		if(!this.isComputing())
			return;
		this.setState(State.ERROR);
	}
	
	@Override
	synchronized public void terminate(boolean isSolvable) {
		if(!this.isComputing())
			return;
		this.setResult(isSolvable);
		this.setState(State.TERMINATED);
	}

}
