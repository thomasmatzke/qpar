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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.dom.ComputationState;
import qpar.common.rmi.InterpretationData;
import qpar.common.rmi.SlaveRemote;
import qpar.common.rmi.TQbfRemote;

public class TQbf extends Observable implements TQbfRemote {
	private static final Logger LOGGER = LoggerFactory.getLogger(TQbf.class);

	private ArrayList<Integer> falseVars = new ArrayList<Integer>();
	private ArrayList<Integer> trueVars = new ArrayList<Integer>();

	private final Map<ComputationState, Date> history = new HashMap<ComputationState, Date>();
	private String id;
	private final Job job;

	private SlaveRemote slave;

	private String solverId = null;

	private ComputationState state = ComputationState.NEW;

	private long timeout;

	private long overheadMillis, solverMillis;

	private boolean result;

	public static HashMap<String, TQbf> tqbfs = new HashMap<String, TQbf>();

	public TQbf(final String tqbfId, final Job job, final String solverId, final ArrayList<Integer> trueVars,
			final ArrayList<Integer> falseVars, final long timeout, final byte[] serializedFormula) throws RemoteException {
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
		return this.result;
	}

	public void setResult(final boolean result) {
		this.result = result;
	}

	public boolean getResult() {
		return this.result;
	}

	public long getOverheadMillis() {
		return this.overheadMillis;
	}

	public void setOverheadMillis(final long overheadMillis) {
		this.overheadMillis = overheadMillis;
	}

	public long getSolverMillis() {
		return this.solverMillis;
	}

	public void setSolverMillis(final long solverMillis) {
		this.solverMillis = solverMillis;
	}

	synchronized public void abort() {
		switch (this.getState()) {
		case NEW:
			this.setState(ComputationState.DONTSTART);
			break;
		case COMPUTING:
			try {
				this.slave.abortFormula(this.getId());
			} catch (RemoteException e) {
				LOGGER.error("", e);
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
			LOGGER.error("Unexpected TQbf state: " + this.getState());
			assert (false);
		}
	}

	public long getComputationTime() {
		if (this.isComputing()) {
			return new Date().getTime() - this.history.get(ComputationState.COMPUTING).getTime();
		} else if (this.isTerminated() || this.isMerged()) {
			if (this.history.get(ComputationState.TERMINATED) == null) {
				LOGGER.info("terminated null. state: " + this.getState());
			}
			if (this.history.get(ComputationState.COMPUTING) == null) {
				LOGGER.info("COMPUTING null state: " + this.getState());
			}
			return this.history.get(ComputationState.TERMINATED).getTime() - this.history.get(ComputationState.COMPUTING).getTime();
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
		return this.falseVars;
	}

	public String getId() {
		return this.id;
	}

	public String getJobId() {
		return this.job.id;
	}

	public SlaveRemote getSlave() {
		return this.slave;
	}

	public String getSolverId() {
		return this.solverId;
	}

	synchronized public ComputationState getState() {
		return this.state;
	}

	public long getTimeout() {
		return this.timeout;
	}

	/**
	 * getter for the trueVars ArrayList
	 * 
	 * @return the ArrayList of true-assigned vars
	 */
	public ArrayList<Integer> getTrueVars() {
		return this.trueVars;
	}

	public InterpretationData getWorkUnit() throws RemoteException {
		InterpretationData id = new InterpretationData(this.job.serializedFormula, this.trueVars, this.falseVars);
		return id;
	}

	synchronized public boolean isAborted() {
		return this.state == ComputationState.ABORTED ? true : false;
	}

	synchronized public boolean isComputing() {
		return this.state == ComputationState.COMPUTING ? true : false;
	}

	synchronized public boolean isDontstart() {
		return this.state == ComputationState.DONTSTART ? true : false;
	}

	synchronized public boolean isError() {
		return this.state == ComputationState.ERROR ? true : false;
	}

	synchronized public boolean isMerged() {
		return this.state == ComputationState.MERGED ? true : false;
	}

	synchronized public boolean isNew() {
		return this.state == ComputationState.NEW ? true : false;
	}

	synchronized public boolean isTerminated() {
		return this.state == ComputationState.TERMINATED ? true : false;
	}

	synchronized public boolean isTimeout() {
		return this.state == ComputationState.TIMEOUT ? true : false;
	}

	public void setId(final String id) {
		this.id = id;
	}

	synchronized public void setMerged() {
		if (!this.isTerminated()) {
			throw new IllegalStateException("Cant merge a non TERMINATED tqbf. State was: " + this.getState());
		}
		this.setState(ComputationState.MERGED);
	}

	public void setSlave(final SlaveRemote slave) {
		this.slave = slave;
	}

	public void setSolverId(final String solverId) {
		this.solverId = solverId;
	}

	synchronized public void setState(final ComputationState state) {
		if (this.isAborted() || this.isMerged() || this.isError() || this.isTimeout()) {
			LOGGER.error("Wanted to set state from " + this.getState() + " to " + state);
			throw new IllegalStateException();
		}

		LOGGER.info("Tqbf " + this.getId() + " to change state from " + this.state + " to " + state);
		this.state = state;
		this.history.put(state, new Date());
		this.setChanged();

		Master.globalThreadPool.execute(new Runnable() {

			public void run() {
				TQbf.this.notifyObservers();
			}
		});

		// notifyAll();
	}

	public void setTimeout(final long timeout) {
		this.timeout = timeout;
	}

	synchronized public void timeout() {
		if (!this.isComputing()) {
			throw new IllegalStateException("Tqbf must be computing to change to state TIMEOUT. State was: " + this.getState());
		}
		this.setState(ComputationState.TIMEOUT);
	}

	synchronized public void error() {
		LOGGER.info("received error in state: " + this.getState());
		if (!this.isComputing()) {
			return;
		}
		this.setState(ComputationState.ERROR);
	}

	synchronized public void terminate(final boolean isSolvable) {
		if (!this.isComputing()) {
			return;
		}
		this.setResult(isSolvable);
		this.setState(ComputationState.TERMINATED);
	}

}
