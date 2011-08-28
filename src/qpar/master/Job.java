package qpar.master;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;


import org.apache.log4j.Logger;

import qpar.common.Configuration;
import qpar.master.heuristic.Heuristic;
import qpar.master.heuristic.HeuristicFactory;

public class Job extends Observable implements Observer {
	private static final long serialVersionUID = 9045629901672956321L;

	public enum State {
		READY, RUNNING, COMPLETE, ERROR, TIMEOUT
	}

	private volatile State state;

	private static volatile int idCounter = 0;
	private static Map<String, Job> jobs = new HashMap<String, Job>();
	private static AbstractTableModel tableModel;
	static Logger logger = Logger.getLogger(Job.class);

	private boolean isSolvable;
	private long timeout = 0, setupTime = 0;
	
	public int usedCores = 0, handledResults = 0;
	
	private Qbf formula;
	public DTNode decisionRoot = null;
	volatile public byte[] serializedFormula;
	public List<TQbf> subformulas;

	private TQbf completingTqbf = null;
	
	public String id, inputFileString, outputFileString, solverId;

	private HashMap<State, Date> history = new HashMap<State, Date>();

	private Heuristic heuristic;
	
	public LinkedHashSet<Integer> variableOrder;
	
	public Job(String inputFile, String outputFile, String solverId, Heuristic h, long timeout, int maxCores) throws RemoteException {
		this.setSetupTime(new Date().getTime());
		this.usedCores = maxCores;
		this.setTimeout(timeout);
		this.setId(allocateJobId());
		addJob(this);
		this.setInputFileString(inputFile);
		this.setOutputFileString(outputFile);
		this.setSolver(solverId);
		this.setState(State.READY);
		this.heuristic = h;
		
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
			// We only want to serialize the tree once
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out;
			out = new ObjectOutputStream(bos);
			out.writeObject(formula.root);
			out.close();
			this.serializedFormula = bos.toByteArray();
			bos.close();
			subformulas = splitQbf(h);
		} catch (IOException e1) {
			logger.error("Couldnt split formula", e1);
			this.setState(State.ERROR);
			return;
		}	
		
		for (TQbf tqbf : subformulas) {
			tqbf.addObserver(this);
		}
		
		logger.info("Job created. \n" + "	JobId:        " + this.id + "\n" + "	HeuristicId:  " + this.heuristic.getId() + "\n" + "	SolverId:     "
				+ this.getSolver() + "\n" + "	#Cores:       " + this.usedCores + "\n" + "	Inputfile:    " + this.getInputFileString() + "\n"
				+ "	Outputfile:   " + this.getOutputFileString() + "\n" + "	Timeout:  " + this.timeout + "\n"+ "	Variable Order:  " + this.variableOrder + "\n");
		
		// Dont need the tree anymore
		this.formula = null;
		
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
			
	synchronized public void error() {
		logger.error("Tqbf computation returned with error");
		this.abortComputations();
		this.setState(State.ERROR);
		this.freeResources();
	}
			
	synchronized public void complete(boolean result) {
		logger.info("Job complete. Resolved to: " + result);
		this.setResult(result);
		this.setState(State.COMPLETE);
				
		this.abortComputations();

		// Write the results to a file
		// But only if we want that. In case of a evaluation
		// the outputfile is set to null
		if (this.getOutputFileString() != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(this.getOutputFileString()));
				out.write(resultText());
				out.flush();
				out.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		
		if (Job.getTableModel() != null)
			Job.getTableModel().fireTableDataChanged();
			
		this.freeResources();
	}
	
	synchronized public void timeout() {
		if (this.isComplete() || this.isError()) {
			return;
		}
		logger.info("Timeout reached. Job: " + this.id + ", Timeout: " + this.timeout);
		abortComputations();
		this.setState(State.TIMEOUT);
		this.freeResources();
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

	public List<TQbf> splitQbf(Heuristic h) throws IOException {
		logger.debug("Splitting into " + usedCores + " subformulas...");
		long start = System.currentTimeMillis();
		logger.debug("Generating variable order...");
		long heuristicTime = System.currentTimeMillis();
		variableOrder = h.getVariableOrder(this.formula);
		heuristicTime = System.currentTimeMillis() - heuristicTime;
		logger.debug("Variable order generated. Took " + heuristicTime / 1000 + " seconds.");
		logger.debug("Heuristic returned variable-assignment order: " + variableOrder);

		int leafCtr = 1;
		ArrayDeque<DTNode> leaves = new ArrayDeque<DTNode>();
		decisionRoot = new DTNode(DTNode.DTNodeType.TQBF);
		leaves.addFirst(decisionRoot);

		// Generate the tree
		logger.debug("Generating decision tree...");
		
		
		while (leafCtr < usedCores) {
			DTNode leaf = leaves.pollLast();
			Integer splitVar = new ArrayList<Integer>(variableOrder).get(leaf.getDepth());
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

		logger.debug(String.format("DT-Leaves: %d, Desired Cores: %d, Variable Order size: %d", leaves.size(), usedCores, variableOrder.size()));
		
		assert (leaves.size() == usedCores);

		logger.debug("Generating TransmissionQbfs...");
		List<TQbf> tqbfs = new ArrayList<TQbf>();
	
		// Generate ids for leaves and corresponding tqbfs
		int idCtr = 0;
		for (DTNode node : leaves) {
			String tqbfId = this.id + "." + Integer.toString(idCtr++);
			node.setId(tqbfId);
			TQbf tqbf = new TQbf(tqbfId, this, this.solverId, node.variablesAssignedTrue, node.variablesAssignedFalse, this.timeout, serializedFormula);
			tqbfs.add(tqbf);
		}
		assert (tqbfs.size() == usedCores);
		long end = System.currentTimeMillis();
		logger.debug("Formula splitted. Took " + (end - start) / 1000 + " seconds.");
		logger.debug("\n" + decisionRoot.dump());
		return tqbfs;
	}

	synchronized private boolean mergeQbf(String tqbfId, boolean result) {
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
		this.setState(State.ERROR);
	}

	public void setResult(boolean r) {
		this.isSolvable = r;
	}

	public boolean getResult() {
		return isSolvable;
	}

	public long totalMillis() {
		if(!this.isTimeout() && !this.isComplete()) {
			logger.error("State was expected TIMEOUT or COMPLETE. But was: " + this.getStatus());
			throw new IllegalStateException();
		}			
				
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
				+ this.getSolver() + "\n" + "Heuristic: " + this.heuristic.getId() + "\n" + "Result: " + (this.getResult() ? "Solvable" : "Not Solvable") + "\n";

		return txt.replaceAll("\n", System.getProperty("line.separator"));
	}
	
	synchronized public void handleResult(TQbf terminatedTqbf) {
		handledResults++;
		if(!this.isRunning())
			return;

		/*
		 * If we are in benchmarking mode we wait for all tqbfs, because they
		 * could start computing at very different timestamps. For benchmarking
		 * we want to assume they start at the same time, and merge in order of
		 * completion. In production we don't give a **** and join as soon as
		 * any results are available.
		 */
		boolean solved = false;
		if (Configuration.isBenchmarkMode()) {
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
			solved = mergeQbf(terminatedTqbf.getId(), terminatedTqbf.getResult());
			this.completingTqbf = terminatedTqbf;
		}

		logger.info("Result of tqbf(" + terminatedTqbf.getId() + ") merged into Qbf of Job " + this.id + " (" + terminatedTqbf.getResult() + ")");
		// this.formulaDesignations.remove(r.tqbfId);
		if (solved) {
			complete(decisionRoot.getTruthValue());
		} else {
			if (handledResults == this.usedCores) {
				StringBuffer errorMsg = new StringBuffer();
				errorMsg.append("No result on merge. Handled results: " + handledResults + ", " +
								 "Used cores: " + this.usedCores + "\n");
				errorMsg.append("Job status: " + this.getStatus() + "\n");
				for(TQbf tqbf : this.subformulas) {
					errorMsg.append("Tqbf " + tqbf.getId() + " state is " + tqbf.getState() + "\n");
				}
				
				// Received all subformulas but still no result...something is
				// wrong
				logger.error(errorMsg.toString());
				logger.error("Dumping decisiontree: \n" + decisionRoot.dump());
			}
		}

	}

	private boolean mergeFinishedTqbfsInOrder() {
		ArrayList<TQbf> finished = new ArrayList<TQbf>();
		for (TQbf tqbf : this.subformulas) {
			if(tqbf.isTerminated())
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
			boolean isCompleted = mergeQbf(tqbf.getId(), tqbf.getResult());
			tqbf.setMerged();
			if (isCompleted) {
				this.completingTqbf = tqbf;
				return true;
			}
		}

		return false;
	}

	private void abortComputations() {
		for (TQbf tqbf : this.subformulas) {
			tqbf.abort();
		}
	}

//	public Qbf getFormula() {
//		return formula;
//	}
	
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

	synchronized private void setState(State state) {
		logger.debug("Job " + this.id + " to change state from " + this.state + " to " + state);
		this.state = state;
		this.history.put(state, new Date());
		setChanged();
		notifyObservers();
		notifyAll();
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
				added += tqbf.getSolverMillis();
				terminatedCount++;
			}				
		}

		if(terminatedCount == 0)
			return 0;
		double mean = (double) added / (double) terminatedCount;
		return mean;
	}

	public long maxSolverTime() {
		if (!this.isComplete() && !this.isTimeout())
			throw new IllegalStateException("Job not in state COMPLETE or TIMEOUT.");

		long maxTime = 0;
		for (TQbf tqbf : this.subformulas) {
			if(tqbf.isTerminated() && tqbf.getSolverMillis() > maxTime)
				maxTime = tqbf.getSolverMillis();
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
				added += tqbf.getOverheadMillis();
				terminated++;
			}				
		}

		if(terminated == 0)
			return 0;
		double mean = (double) added / (double) terminated;
		return mean;
	}
	
	/**
	 * Observes its tqbfs
	 */
	@Override
	synchronized public void update(Observable o, Object o1) {
		if (o instanceof TQbf) {
			TQbf tqbf = (TQbf) o;
			switch (tqbf.getState()) {
				case COMPUTING:
				case ABORTED:
				case MERGED:
					break;
				case TERMINATED:
					this.handleResult(tqbf);
					break;
				case DONTSTART:
					break;
				case TIMEOUT:
					this.timeout();
					break;
				case ERROR:
					this.error();
					break;
				case NEW:				
				default:
					assert (false);
			}
		}
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

	private void freeResources() {
		this.formula = null;
		this.decisionRoot = null;
		this.serializedFormula = null;
	}

}
