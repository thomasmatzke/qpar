package main.java.slave;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import main.java.logic.TransmissionQbf;
import main.java.rmi.Result;
import main.java.slave.solver.ResultHandler;
import main.java.slave.solver.Solver;
import main.java.slave.solver.SolverFactory;

import org.apache.log4j.Logger;

public class ComputationStateMachine implements ResultHandler{

	public static volatile ConcurrentMap<String, ComputationStateMachine> computations =  new ConcurrentHashMap<String, ComputationStateMachine>();
	
	static Logger logger = Logger.getLogger(ComputationStateMachine.class);
	
	public enum State { START, COMPUTING, COMPLETE, ABORTED }
	
	volatile private State state = State.START;
	
	private Solver solver = null;
	private TransmissionQbf tqbf = null;
	private String solverId = null;
	public String tqbfId = null;
	
	public ComputationStateMachine(TransmissionQbf tqbf) {
		this.tqbf = tqbf;
		this.tqbfId = tqbf.getId();
		this.solverId = tqbf.solverId;
	}
	
	synchronized public State getState() {
		return state;
	}
	
	synchronized public void startComputation() {
		switch(state) {
			case START:				
				compute();
				break;
			default:
				break;				
		}
		state = State.COMPUTING;
	}
	
	synchronized public void abortComputation() {
		switch(state) {
			case COMPUTING:
				abort();
				break;
			default:
				break;				
		}
		state = State.ABORTED;
	}
	
	synchronized public void completeComputation(Result r) {
		switch(state) {
			case COMPUTING:
				complete(r);
				break;
			default:
				break;				
		}
		state = State.COMPLETE;
	}
	
	private void compute() {
		logger.info("Computing formula " + tqbfId + "...");
		solver = SolverFactory.getSolver(solverId, this, tqbf);
		this.tqbf = null;
		solver.getThread().start();
		try {
			Slave.master.notifyComputationStarted(tqbfId);
		} catch (RemoteException e) {
			logger.error("RMI fail", e);
		}
	}
	
	private void complete(Result r) {
		logger.info("Completed formula " + r.tqbfId + "...");
		new Thread(new ResultTransport(Slave.master, r)).start();
		ComputationStateMachine.computations.remove(this.tqbfId);		
	}

	private void abort() {
		logger.info("Aborting formula " + tqbfId + "...");
		solver.kill();
		ComputationStateMachine.computations.remove(this.tqbfId);
	}

	@Override
	public void handleResult(Result r) {
		this.completeComputation(r);
	}
	
	
}
