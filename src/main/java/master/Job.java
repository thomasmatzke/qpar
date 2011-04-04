package main.java.master;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import main.java.QPar;
import main.java.logic.DTNode;
import main.java.logic.Qbf;
import main.java.logic.heuristic.Heuristic;
import main.java.logic.heuristic.HeuristicFactory;
import main.java.rmi.RemoteObservable;
import main.java.rmi.RemoteObserver;
import main.java.rmi.Result;
import main.java.rmi.WrappedObserver;
import main.java.scheduling.Distributor;

import org.apache.log4j.Logger;

public class Job extends Observable implements RemoteObserver, RemoteObservable {

	public enum State {
		READY, RUNNING, COMPLETE, ERROR, TIMEOUT
	}

	private volatile State state;

	private static volatile int idCounter = 0;
	private static Map<String, Job> jobs = new HashMap<String, Job>();
	private static AbstractTableModel tableModel;
	static Logger logger = Logger.getLogger(Job.class);

	private boolean jobResult;
	private long timeout = 0, setupTime = 0;
	
	public int usedCores = 0;
	
	private Qbf formula;
	public DTNode decisionRoot = null;
	public byte[] serializedFormula;
	public List<TQbf> subformulas;

	private TQbf completingTqbf = null;
	
	public String heuristic, id, inputFileString, outputFileString, solverId;

	private HashMap<State, Date> history = new HashMap<State, Date>();

	private List<Result> results = new ArrayList<Result>();

	public Job(String inputFile, String outputFile, String solverId, String heuristicId, long timeout, int maxCores) throws RemoteException {
		this.setSetupTime(new Date().getTime());
		addJob(this);
		
		this.usedCores = maxCores;
		this.setTimeout(timeout);
		this.setId(allocateJobId());
		this.setInputFileString(inputFile);
		this.setOutputFileString(outputFile);
		this.setSolver(solverId);
		this.setHeuristic(heuristicId);
		this.setState(State.READY);
		
		logger.info("Job created. \n" + "	JobId:        " + this.id + "\n" + "	HeuristicId:  " + this.getHeuristic() + "\n" + "	SolverId:     "
				+ this.getSolver() + "\n" + "	#Cores:       " + this.usedCores + "\n" + "	Inputfile:    " + this.getInputFileString() + "\n"
				+ "	Outputfile:   " + this.getOutputFileString() + "\n" + "	Timeout:  " + this.timeout + "\n");

		if (tableModel != null)
			tableModel.fireTableDataChanged();
		try {
			this.formula = new Qbf(inputFileString);
		} catch (Exception e) {
			logger.error(this.inputFileString, e);
			this.setState(State.ERROR);
			return;
		}

		try {
			subformulas = splitQbf();
		} catch (IOException e1) {
			logger.error("Couldnt split formula", e1);
			this.setState(State.ERROR);
			return;
		}

		for (TQbf tqbf : subformulas) {
			tqbf.addObserver(this);
//			tqbf.setSolverId(this.solverId);
//			tqbf.setJobId(this.id);
//			tqbf.setTimeout(this.timeout);
		}
		this.setSetupTime(new Date().getTime() - this.getSetupTime());
	}

	private static void addJob(Job job) {
		jobs.put(job.id, job);
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
		logger.info("Job added. JobId: " + job.id);
	}

	synchronized private static String allocateJobId() {
		idCounter++;
		return Integer.valueOf(idCounter).toString();
	}

	public static Map<String, Job> getJobs() {
		if (jobs == null) {
			jobs = new HashMap<String, Job>();
		}
		return jobs;
	}

	synchronized public void triggerTimeout() {
		if (this.isComplete() || this.isError()) {
			return;
		}
		logger.info("Timeout reached. Job: " + this.id + ", Timeout: " + this.timeout);
		abortComputations();
		this.setState(State.TIMEOUT);

		notifyAll();

	}

	public void startBlocking() {
		this.start();
		synchronized (this) {
			while (this.state == State.RUNNING) {
				try {
					this.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void start() {
//		new TimeoutTimer(this.timeout, this);
		this.setState(State.RUNNING);
		logger.info("Job started " + this.id + " ...\n" + "	Started at:  " + this.history.get(State.RUNNING) + "\n" + "	UsedCores: " + this.usedCores);

		Distributor.instance().scheduleJob(this);
	}

	public List<TQbf> splitQbf() throws IOException {
		logger.debug("Splitting into " + usedCores + " subformulas...");
		long start = System.currentTimeMillis();
		Heuristic h = HeuristicFactory.getHeuristic(this.getHeuristic(), formula);
		logger.info("Generating variable order...");
		long heuristicTime = System.currentTimeMillis();
		Integer[] order = h.getVariableOrder().toArray(new Integer[0]);
		heuristicTime = System.currentTimeMillis() - heuristicTime;
		logger.info("Variable order generated. Took " + heuristicTime / 1000 + " seconds.");
		logger.debug("Heuristic returned variable-assignment order: " + Arrays.toString(order));

		int leafCtr = 1;
		ArrayDeque<DTNode> leaves = new ArrayDeque<DTNode>();
		decisionRoot = new DTNode(DTNode.DTNodeType.TQBF);
		leaves.addFirst(decisionRoot);

		// Generate the tree
		logger.debug("Generating decision tree...");
		while (leafCtr < usedCores) {
			DTNode leaf = leaves.pollLast();
			Integer splitVar = order[leaf.getDepth()];
			if (formula.aVars.contains(splitVar)) {
				leaf.setType(DTNode.DTNodeType.AND);
			} else if (formula.eVars.contains(splitVar)) {
				leaf.setType(DTNode.DTNodeType.OR);
			}
			DTNode negChild = new DTNode(DTNode.DTNodeType.TQBF);
			negChild.variablesAssignedFalse.add(splitVar);
			negChild.variablesAssignedFalse.addAll(leaf.variablesAssignedFalse);
			negChild.variablesAssignedTrue.addAll(leaf.variablesAssignedTrue);
			negChild.setParent(leaf);
			negChild.getDepth();
			DTNode posChild = new DTNode(DTNode.DTNodeType.TQBF);
			posChild.variablesAssignedTrue.add(splitVar);
			posChild.variablesAssignedFalse.addAll(leaf.variablesAssignedFalse);
			posChild.variablesAssignedTrue.addAll(leaf.variablesAssignedTrue);
			posChild.setParent(leaf);
			posChild.getDepth();
			leaf.addChild(negChild);
			leaf.addChild(posChild);
			leaves.addFirst(negChild);
			leaves.addFirst(posChild);
			leafCtr++;
		}

//		logger.info("\n" + decisionRoot.dump());

		assert (leaves.size() == usedCores);

		logger.debug("Generating TransmissionQbfs...");
		List<TQbf> tqbfs = new ArrayList<TQbf>();

		// We only want to serialize the tree once

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		out = new ObjectOutputStream(bos);
		out.writeObject(formula.root);
		out.close();
		byte[] serializedFormula = bos.toByteArray();

		// Generate ids for leaves and corresponding tqbfs
		int idCtr = 0;
		for (DTNode node : leaves) {
			String tqbfId = this.id + "." + Integer.toString(idCtr++);
			node.setId(tqbfId);
			TQbf tqbf = new TQbf(tqbfId, this.id, this.solverId, node.variablesAssignedTrue, node.variablesAssignedFalse, this.timeout, serializedFormula);
//			tqbf.getFalseVars().addAll(node.variablesAssignedFalse);
//			tqbf.getTrueVars().addAll(node.variablesAssignedTrue);
//			tqbf.setRootNode(formula.root);
//			tqbf.serializedFormula = serializedFormula;
//			tqbf.setId(tqbfId);
//			tqbf.setEVars(formula.eVars);
//			tqbf.setAVars(formula.aVars);
//			Vector<Integer> tmpVars = new Vector<Integer>();
//			tmpVars.addAll(formula.aVars);
//			tmpVars.addAll(formula.eVars);
//			tqbf.setVars(tmpVars);
			tqbfs.add(tqbf);
		}
		assert (tqbfs.size() == usedCores);
		long end = System.currentTimeMillis();
		logger.debug("Formula splitted. Took " + (end - start) / 1000 + " seconds.");
		return tqbfs;
	}

	private boolean mergeQbf(String tqbfId, boolean result) {
		logger.info("Merging tqbf " + tqbfId);
		if (decisionRoot.hasTruthValue())
			return true;

		// find the corresponding node in the decisiontree
		DTNode tmp = decisionRoot.getNode(tqbfId);

		if (tmp == null) {
			// The node has been cut off from the root by another reduce()
			// The result is thus irrelevant
			return decisionRoot.hasTruthValue();
		}

		// set the nodes truth value
		tmp.setTruthValue(result);

		// reduce the tree
		tmp.reduce();

		// check the root for a truth value and return
		return decisionRoot.hasTruthValue();
	}

	synchronized public void abort(String why) {
		if (this.getStatus() != State.RUNNING)
			return;

		logger.info("Job abort. Reason: " + why);
		abortComputations();
		// this.freeResources();
		this.setState(State.ERROR);
	}

	public void setResult(boolean r) {
		this.jobResult = r;
	}

	public boolean getResult() {
		return jobResult;
	}

	public long totalMillis() {
		if(!this.isTimeout() && !this.isComplete())
			throw new IllegalStateException();
		
		long total = 0;
		total += this.getSetupTime();
		if(this.isTimeout())
			total += this.timeout * 1000;
		else
			total += this.completingTqbf.getComputationTime();
		
		return total;
	}

	private String resultText() {
		String txt;
		txt = "Job Id: " + this.id + "\n" + "State RUNNING at: " + this.history.get(State.RUNNING) + "\n" + "State COMPLETE at: "
				+ this.history.get(State.COMPLETE) + "\n" + "Total secs: " + totalMillis() / 1000 + "\n" + "In millis: " + totalMillis() + "\n" + "Solver: "
				+ this.getSolver() + "\n" + "Heuristic: " + this.getHeuristic() + "\n" + "Result: " + (this.getResult() ? "Solvable" : "Not Solvable") + "\n";

		return txt.replaceAll("\n", System.getProperty("line.separator"));
	}

	
	// TODO: move this crap to TQbf
	synchronized public void handleResult(Result r) {

		this.results.add(r);
		if (this.getStatus() != State.RUNNING)
			return;

		if (r.type == Result.Type.ERROR) {
			logger.error("Slave returned error for subformula: " + r.tqbfId, r.exception);

			this.abortComputations();
			this.setState(State.ERROR);
			return;
		}

		/*
		 * If we are in benchmarking mode we wait for all tqbfs, because they
		 * could start computing at very different timestamps. For benchmarking
		 * we want to assume they start at the same time, and merge in order of
		 * completion. In production we don't give a **** and join as soon as
		 * any results are available.
		 */
		boolean solved = false;
		if (QPar.benchmarkMode) {
			/*
			 * If all remaining tqbfs are already computing longer than those
			 * which are finished, then we can merge the finished ones (in order
			 * of completion)
			 */
			long maxTimeOfFinished = 0;
			long minTimeOfComputing = Long.MAX_VALUE;

			for (TQbf tqbf : this.subformulas) {
				if (tqbf.isComputing() && tqbf.getComputationTime() < minTimeOfComputing) {
					minTimeOfComputing = tqbf.getComputationTime();
				} else if (tqbf.isTerminated() && tqbf.getComputationTime() > maxTimeOfFinished) {
					maxTimeOfFinished = tqbf.getComputationTime();
				}
			}
			logger.info("maxTimeFinished: " + maxTimeOfFinished + ", minTimeComputing: " + minTimeOfComputing);
			if (minTimeOfComputing > maxTimeOfFinished) {
				solved = this.mergeFinishedTqbfsInOrder();
			}

		} else {
			solved = mergeQbf(r.tqbfId, r.getResult());
		}

		logger.info("Result of tqbf(" + r.tqbfId + ") merged into Qbf of Job " + this.id + " (" + r.type + ")");
		// this.formulaDesignations.remove(r.tqbfId);
		if (solved) {
			fireJobCompleted(decisionRoot.getTruthValue());
		} else {
			if (results.size() == this.usedCores) {
				// Received all subformulas but still no result...something is
				// wrong
				logger.fatal("Merging broken!");
				logger.fatal("Dumping decisiontree: \n" + decisionRoot.dump());
				throw new RuntimeException("Merging broken!");
			}
		}

	}

	private boolean mergeFinishedTqbfsInOrder() {
		boolean solved = false;
		ArrayList<TQbf> finished = new ArrayList<TQbf>();
		for (TQbf tqbf : this.subformulas) {
			if (tqbf.getState() != TQbf.State.TERMINATED)
				continue;
			finished.add(tqbf);
		}

		Collections.sort(finished, new Comparator<TQbf>() {
			@Override
			public int compare(final TQbf arg0, final TQbf arg1) {
				return (int) (arg0.getComputationTime() - arg1.getComputationTime());
			}
		});

		if (finished.size() > 1)
			assert (finished.get(0).getComputationTime() <= finished.get(1).getComputationTime());

		for (TQbf tqbf : finished) {
			if (mergeQbf(tqbf.getId(), tqbf.getResult().getResult())) {
				this.completingTqbf = tqbf;
				return true;
			}
		}

		return solved;
	}

	private void fireJobCompleted(boolean result) {
		this.setState(State.COMPLETE);
		this.setResult(result);

		logger.info("Job complete. Resolved to: " + result + ". Aborting computations.");
		this.abortComputations();

		// Write the results to a file
		// But only if we want that. In case of a evaluation
		// the outputfile is set to null
		if (this.getOutputFileString() != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(this.getOutputFileString()));
				out.write(resultText());
				out.flush();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		// if (Shell.getWaitfor_jobid().equals(this.id)) {
		// synchronized (Master.getShellThread()) {
		// Master.getShellThread().notify();
		// }
		// }

		if (Job.getTableModel() != null)
			Job.getTableModel().fireTableDataChanged();
				
		synchronized (this) {
			notifyAll();
		}

	}

	private void abortComputations() {
		for (TQbf tqbf : this.subformulas) {
			tqbf.abort();
		}
	}

	public Qbf getFormula() {
		return formula;
	}

	public String getHeuristic() {
		return heuristic;
	}

	public String getInputFileString() {
		return inputFileString;
	}

	public String getOutputFileString() {
		return outputFileString;
	}

	public String getSolver() {
		return solverId;
	}

	public State getStatus() {
		return state;
	}

	public void setFormula(Qbf formula) {
		this.formula = formula;
	}

	public void setHeuristic(String heuristic) {
		this.heuristic = heuristic;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setInputFileString(String inputFileString) {
		this.inputFileString = inputFileString;
	}

	public void setOutputFileString(String outputFileString) {
		this.outputFileString = outputFileString;
	}

	public void setSolver(String solver) {
		this.solverId = solver;
	}

	public void setState(State state) {
		logger.info("Job " + this.id + " to change state from " + this.state + " to " + state);
		this.state = state;
		this.history.put(state, new Date());
		setChanged();
		notifyObservers();
		if (Job.getTableModel() != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Job.getTableModel().fireTableDataChanged();
				}
			});
		}
	}

	public static AbstractTableModel getTableModel() {
		return tableModel;
	}

	public static void setTableModel(AbstractTableModel tableModel) {
		Job.tableModel = tableModel;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public double meanSolverTime() {
		if(!this.isComplete() && !this.isTimeout())
			throw new IllegalStateException("Job not in state COMPLETE or TIMEOUT.");

		int terminatedCount = 0;
		long added = 0;
		for (TQbf tqbf : this.subformulas) {
			if(tqbf.isTerminated()) {
				added += tqbf.getResult().solverTime;
				terminatedCount++;
			}				
		}

		double mean = (double) added / (double) terminatedCount;
		return mean;
	}

	public long maxSolverTime() {
		if (!this.isComplete() && !this.isTimeout())
			throw new IllegalStateException("Job not in state COMPLETE or TIMEOUT.");

		long maxTime = 0;
		for (TQbf tqbf : this.subformulas) {
			if(tqbf.isTerminated() && tqbf.getResult().solverTime > maxTime)
				maxTime = tqbf.getResult().solverTime;
		}
		return maxTime;
	}

	public double meanOverheadTime() {
		if (!this.isComplete() && !this.isTimeout())
			throw new IllegalStateException("Job not in state COMPLETE or TIMEOUT.");

		int terminated = 0;
		long added = 0;
		for (TQbf tqbf : this.subformulas) {
			if(tqbf.isTerminated()) {
				added += tqbf.getResult().overheadTime;
				terminated++;
			}				
		}

		double mean = (double) added / (double) terminated;
		return mean;
	}

	@Override
	public void addObserver(RemoteObserver o) throws RemoteException {
		WrappedObserver mo = new WrappedObserver(o);
		addObserver(mo);
	}

	/**
	 * Observes its tqbfs
	 */
	@Override
	public void update(Object o, Object o1) throws RemoteException {
		if (o instanceof TQbf) {
			TQbf tqbf = (TQbf) o;
			switch (tqbf.getState()) {
				case COMPUTING:
				case ABORTED:
					break;
				case TERMINATED:
					this.handleResult(tqbf.getResult());
					break;
				case DONTSTART:
					break;
				case TIMEOUT:
					this.triggerTimeout();
					break;
				case NEW:
				default:
					assert (false);
			}
		}
//		synchronized(this) {
//			notifyAll();
//		}
	}

	public HashMap<State, Date> getHistory() {
		return history;
	}

	public void setHistory(HashMap<State, Date> history) {
		this.history = history;
	}

	public long getSetupTime() {
		return setupTime;
	}

	public void setSetupTime(long setupTime) {
		this.setupTime = setupTime;
	}
		
	public boolean isReady() {
		return (this.getStatus().equals(State.READY) ? true : false);
	}
	public boolean isRunning() {
		return (this.getStatus().equals(State.RUNNING) ? true : false);
	}
	public boolean isComplete() {
		return (this.getStatus().equals(State.COMPLETE) ? true : false);
	}
	public boolean isError() {
		return (this.getStatus().equals(State.ERROR) ? true : false);
	}
	public boolean isTimeout() {
		return (this.getStatus().equals(State.TIMEOUT) ? true : false);
	}

}
