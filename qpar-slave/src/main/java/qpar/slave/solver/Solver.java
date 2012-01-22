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
package qpar.slave.solver;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.Configuration;
import qpar.common.dom.ComputationState;
import qpar.common.rmi.InterpretationData;
import qpar.common.rmi.MasterRemote;
import qpar.common.rmi.TQbfRemote;
import qpar.slave.Slave;
import qpar.slave.tree.ReducedInterpretation;

/**
 * Basic interface which all solvers have to implement. To appear in the list of
 * solvers, you have to add your solver in the SolverFactory
 * 
 * @author thomasm
 * 
 */
public class Solver implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Solver.class);

	public static ExecutorService solverThreadPool = Executors.newFixedThreadPool(Slave.availableProcessors);
	private static ExecutorService pluginThreadPool = Executors.newFixedThreadPool(Slave.availableProcessors);
	public static ConcurrentHashMap<String, Solver> solvers = new ConcurrentHashMap<String, Solver>();

	protected TQbfRemote tqbf;
	protected MasterRemote master;
	protected String tqbfId = null, jobId = null, solverId = null;
	protected long timeout;
	public Date overheadStartedAt = null;
	protected Date overheadStoppedAt = null;
	protected InterpretationData interpretationData;
	protected ReducedInterpretation reducedInterpretation;
	final SolverPlugin plugin;
	volatile protected boolean killSignalReceived = false, pluginStarted = false, timedOut = false;

	private Timer t;

	private Date pluginStoppedAt, pluginStartedAt;

	Lock killLock = new ReentrantLock();

	public Solver(final TQbfRemote tqbf, final MasterRemote master) throws RemoteException, UnknownHostException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		this.master = master;
		this.tqbf = tqbf;
		this.tqbfId = tqbf.getId();
		this.jobId = tqbf.getJobId();
		this.timeout = tqbf.getTimeout();
		this.solverId = tqbf.getSolverId();
		this.plugin = SolverPluginFactory.getSolver(this.solverId);
	}

	@Override
	public void run() {

		synchronized (this.tqbf) {
			try {
				if (!this.tqbf.isNew()) {
					return;
				}
				Solver.solvers.put(this.tqbfId, this);
				this.tqbf.setState(ComputationState.COMPUTING);
			} catch (RemoteException e) {
				LOGGER.error("", e);
			}
		}

		try {
			if (this.killSignalReceived) {
				return;
			}
			this.overheadStartedAt = new Date();
			this.interpretationData = this.tqbf.getWorkUnit();
			if (this.interpretationData.getRootNode() == null) {
				// data was nulled means job is done
				return;
				// throw new
				// IllegalStateException("Rootnode of InterpretationData was null.");
			}
			this.reducedInterpretation = new ReducedInterpretation(this.interpretationData);
			this.overheadStoppedAt = new Date();

			if (this.killSignalReceived) {
				return;
			}
			if (this.reducedInterpretation.isTruthValue()) {
				LOGGER.debug("Formula " + this.tqbfId + " collapsed");
				this.terminate(this.reducedInterpretation.getTruthValue(), 0, this.overheadMillis());
				return;
			}
			if (Slave.configuration.getProperty(Configuration.RESULT_CACHING, Boolean.class)) {
				Boolean cached = this.master.getCachedResult(this.reducedInterpretation.getTreeHash());
				if (cached != null) {
					this.terminate(cached, 0, this.overheadStoppedAt.getTime() - this.overheadStartedAt.getTime());
					return;
				}
			}

			this.killLock.lock();
			if (this.killSignalReceived) {
				// we have been killed do nothing
			} else {
				// we havent been killed. start the plugin and set the flag
				this.pluginStarted = true;

				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						LOGGER.info("Timing out Tqbf");
						Solver.this.timedOut = true;
						Solver.this.kill();
					}
				};
				this.t = new Timer();
				this.t.schedule(task, this.timeout * 1000);
				this.pluginStartedAt = new Date();
				this.plugin.initialize(this.reducedInterpretation);
				pluginThreadPool.execute(this.plugin);
			}
			this.killLock.unlock();

			Boolean pluginResult = this.plugin.waitForResult();
			this.pluginStoppedAt = new Date();

			if (this.timedOut) {
				this.timeout();
			} else {
				this.terminate(pluginResult, this.solverMillis(), this.overheadMillis());
			}

		} catch (RemoteException e) {
			LOGGER.error(e.getMessage());
		} catch (Exception e) {
			if (this.timedOut) {
				this.timeout();
				return;
			} else if (this.killSignalReceived) {
			} else {
				this.error(e);
			}
		} finally {
			this.tearDown();
		}

	}

	public void kill() {
		this.killLock.lock();
		try {
			if (this.pluginStarted) {
				this.plugin.kill();
			} else {
				this.killSignalReceived = true;
			}
		} finally {
			this.killLock.unlock();
		}
	}

	protected void timeout() {
		try {
			this.tqbf.timeout();
			LOGGER.info("Returned timeout " + this.tqbfId);
		} catch (RemoteException e) {
			LOGGER.error("RMI fail while sending timeout", e);
		}
	}

	protected void terminate(final Boolean isSolvable, final long solverMillis, final long overheadMillis) {
		if (isSolvable == null) {
			return;
		}

		try {
			this.tqbf.setSolverMillis(solverMillis);
			this.tqbf.setOverheadMillis(overheadMillis);
			if (Slave.configuration.getProperty(Configuration.RESULT_CACHING, Boolean.class)) {
				this.master.cacheResult(this.reducedInterpretation.getTreeHash(), isSolvable);
			}
			this.tqbf.terminate(isSolvable);
			LOGGER.info("Returned result " + this.tqbfId + " " + isSolvable);
		} catch (RemoteException e) {
			LOGGER.error("RMI fail while sending result", e);
		}

	}

	protected void error(final Exception e) {
		LOGGER.error("", e);
		LOGGER.error("Reporting error to master");
		try {
			this.tqbf.error();
			LOGGER.info("Returned error " + this.tqbfId);
		} catch (RemoteException re) {
			LOGGER.error("RMI fail while sending error", re);
		}
	}

	protected void tearDown() {
		LOGGER.debug("Tearing down solver of tqbf " + this.tqbfId);
		Solver.solvers.remove(this.tqbfId);
		if (this.t != null) {
			this.t.cancel();
		}
	}

	public long solverMillis() {
		return this.pluginStoppedAt.getTime() - this.pluginStartedAt.getTime();
	}

	public long overheadMillis() {
		return this.overheadStoppedAt.getTime() - this.overheadStartedAt.getTime();
	}

	@Override
	public void finalize() {
		Solver.solvers.remove(this.tqbfId);
	}

}
