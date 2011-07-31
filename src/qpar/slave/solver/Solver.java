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


import org.apache.log4j.Logger;

import qpar.common.Configuration;
import qpar.common.rmi.InterpretationData;
import qpar.common.rmi.TQbfRemote;
import qpar.master.TQbf;
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
	static Logger logger = Logger.getLogger(Solver.class);
	
	public static ExecutorService solverThreadPool = Executors.newFixedThreadPool(Configuration.getAvailableProcessors());
	private static ExecutorService pluginThreadPool = Executors.newFixedThreadPool(Configuration.getAvailableProcessors());
	public static ConcurrentHashMap<String, Solver> solvers = new ConcurrentHashMap<String, Solver>();
		
	protected TQbfRemote tqbf;
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
	
	public Solver(TQbfRemote tqbf) throws RemoteException, UnknownHostException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.tqbf = tqbf;
		this.tqbfId = tqbf.getId();
		this.jobId = tqbf.getJobId();
		this.timeout = tqbf.getTimeout();
		this.solverId = tqbf.getSolverId();
		tqbf.setSlave(Slave.instance());
		this.plugin = SolverPluginFactory.getSolver(this.solverId);
	}

	public void run() {
		
		synchronized(tqbf) {
			try {
				if(!tqbf.isNew())
					return;	
				Solver.solvers.put(tqbfId, this);
				tqbf.setState(TQbf.State.COMPUTING);
			} catch (RemoteException e) {logger.error("", e);}	
		}
		
		try {
			if(this.killSignalReceived)
				return;
			this.overheadStartedAt = new Date();
			interpretationData = tqbf.getWorkUnit();		
			if(interpretationData.getRootNode() == null) {
				// data was nulled means job is done
				return;
//				throw new IllegalStateException("Rootnode of InterpretationData was null.");
			}		
			reducedInterpretation = new ReducedInterpretation(interpretationData);
			this.overheadStoppedAt = new Date();
			
			if(this.killSignalReceived)
				return;
			if(reducedInterpretation.isTruthValue()) {
//				logger.info("Formula " + this.tqbfId + " collapsed");
				this.terminate(reducedInterpretation.getTruthValue(), 0, this.overheadMillis());
				return;
			}
			if(Configuration.isResultCaching()) {
				Boolean cached = Slave.getMaster().getCachedResult(reducedInterpretation.getTreeHash());
				if(cached != null) {
					this.terminate(cached, 0, overheadStoppedAt.getTime() - overheadStartedAt.getTime());
					return;
				}
			}
			
			killLock.lock();
			if(this.killSignalReceived) {
				// we have been killed do nothing
			} else {
				// we havent been killed. start the plugin and set the flag
				this.pluginStarted = true;
				
				TimerTask task = new TimerTask() {
				public void run() {
					logger.info("Timing out");
					timedOut = true;
					kill();
				}};
				t = new Timer();
				t.schedule(task, this.timeout * 1000);
				this.pluginStartedAt = new Date();
				plugin.initialize(reducedInterpretation);
				pluginThreadPool.execute(plugin);
			}
			killLock.unlock();
			
			
			Boolean pluginResult = plugin.waitForResult();
			this.pluginStoppedAt = new Date();
			
			if(timedOut)
				this.timeout();
			else
				this.terminate(pluginResult, solverMillis(), overheadMillis());
						
		} catch (RemoteException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			if(timedOut) {
				timeout();
				return;
			} else if(this.killSignalReceived) {
			} else {
				this.error(e);
			}
		} finally {
			tearDown();
		}
					
	}

	public void kill() {
		killLock.lock();
		try {
			if(this.pluginStarted) {
				plugin.kill();
			} else {
				this.killSignalReceived = true;
			}
		} finally {
			killLock.unlock();
		}
	}
	
	protected void timeout() {
		try {
			this.tqbf.timeout();
			logger.info("Returned timeout " + this.tqbfId);
		} catch (RemoteException e) {
			logger.error("RMI fail while sending timeout", e);
		}
	}
	
	protected void terminate(Boolean isSolvable, long solverMillis, long overheadMillis) {
		if(isSolvable == null)
			return;
			
		try {
			this.tqbf.setSolverMillis(solverMillis);
			this.tqbf.setOverheadMillis(overheadMillis);
			if(Configuration.isResultCaching())
				Slave.getMaster().cacheResult(reducedInterpretation.getTreeHash(), isSolvable);
			this.tqbf.terminate(isSolvable);
			logger.info("Returned result " + this.tqbfId + " " + isSolvable);
		} catch (RemoteException e) {
			logger.error("RMI fail while sending result", e);
		}
		
	}
	
	protected void error(Exception e) {
		logger.error("", e);
		logger.error("Reporting error to master");
		try {
			this.tqbf.error();
			logger.info("Returned error " + this.tqbfId);
		} catch (RemoteException re) {
			logger.error("RMI fail while sending error", re);
		}
	}
	
	protected void tearDown() {
		logger.info("Tearing down solver of tqbf " + this.tqbfId);
		Solver.solvers.remove(this.tqbfId);
		if(this.t != null)
			t.cancel();			
	}
	
	public long solverMillis() {
		return this.pluginStoppedAt.getTime() - this.pluginStartedAt.getTime();
	}
	
	public long overheadMillis() {
		return this.overheadStoppedAt.getTime()	- this.overheadStartedAt.getTime();
	}
	
	
	public void finalize() {
		Solver.solvers.remove(this.tqbfId);
	}

}
