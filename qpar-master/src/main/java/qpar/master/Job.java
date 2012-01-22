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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.Configuration;
import qpar.master.heuristic.Heuristic;

public class Job extends Observable implements Observer {
	public enum State {
		READY, RUNNING, COMPLETE, ERROR, TIMEOUT
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);
	static Map<String, Job> jobs = new HashMap<String, Job>();

	private static int idCounter = 0;

	private static void addJob(final Job job) {
		jobs.put(job.id, job);
		LOGGER.info("Job added. JobId: " + job.id);
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

	private boolean isSolvable;
	private long timeout = 0, setupTime = 0, usedCores = 0, handledResults = 0;
	private TQbf completingTqbf = null;
	private Qbf formula;

	private Heuristic heuristic;
	private HashMap<State, Date> history = new HashMap<State, Date>();
	private State state;
	public DTNode decisionRoot = null;
	public byte[] serializedFormula;

	public List<TQbf> subformulas;

	public LinkedHashSet<Integer> variableOrder;

	public String id, inputFileString, outputFileString, solverId;

	public Job(final String inputFile, final String outputFile, final String solverId, final Heuristic h, final long timeout,
			final int maxCores) throws RemoteException {
		this.setupTime = new Date().getTime();
		this.usedCores = maxCores;
		this.timeout = timeout;
		this.id = allocateJobId();
		addJob(this);
		this.inputFileString = inputFile;
		this.outputFileString = outputFile;
		this.solverId = solverId;
		this.setState(State.READY);
		this.heuristic = h;
		try {
			this.formula = new Qbf(this.inputFileString);
		} catch (Exception e) {
			LOGGER.error(this.inputFileString, e);
			this.setState(State.ERROR);
			return;
		}
		try {
			// We only want to serialize the tree once
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out;
			out = new ObjectOutputStream(bos);
			out.writeObject(this.formula.root);
			out.close();
			this.serializedFormula = bos.toByteArray();
			bos.close();
			this.subformulas = this.splitQbf(h);
		} catch (IOException e1) {
			LOGGER.error("Couldnt split formula", e1);
			this.setState(State.ERROR);
			return;
		}
		for (TQbf tqbf : this.subformulas) {
			tqbf.addObserver(this);
		}

		int neededVariables = (int) Math.ceil(Math.log(this.usedCores) / Math.log(2));

		LOGGER.info("Job created. \n" + "	JobId:        " + this.id + "\n" + "	HeuristicId:  " + this.heuristic.getId() + "\n"
				+ "	SolverId:     " + this.getSolver() + "\n" + "	#Cores:       " + this.usedCores + "\n" + "	Inputfile:    "
				+ this.getInputFileString() + "\n" + "	Outputfile:   " + this.getOutputFileString() + "\n" + "	Timeout:  " + this.timeout
				+ "\n" + "	Variable Order:  " + new ArrayList<Integer>(this.variableOrder).subList(0, neededVariables) + "\n");

		// Dont need the tree anymore
		this.formula = null;

		this.setupTime = new Date().getTime() - this.setupTime;
	}

	synchronized public void abort(final String why) {
		if (this.getStatus() != State.RUNNING) {
			return;
		}

		LOGGER.info("Job abort. Reason: " + why);
		this.abortComputations();
		this.setState(State.ERROR);
	}

	private void abortComputations() {
		for (TQbf tqbf : this.subformulas) {
			tqbf.abort();
		}
	}

	synchronized public void complete(final boolean result) {
		LOGGER.info("Job complete. Resolved to: " + result);
		this.setResult(result);
		this.setState(State.COMPLETE);

		this.abortComputations();

		// Write the results to a file
		// But only if we want that. In case of a evaluation
		// the outputfile is set to null
		if (this.getOutputFileString() != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(this.getOutputFileString()));
				out.write(this.resultText());
				out.flush();
				out.close();
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}

		this.freeResources();
	}

	synchronized public void error() {
		LOGGER.error("Tqbf computation returned with error");
		this.abortComputations();
		this.setState(State.ERROR);
		this.freeResources();
	}

	private void freeResources() {
		this.formula = null;
		this.decisionRoot = null;
		this.serializedFormula = null;
	}

	public HashMap<State, Date> getHistory() {
		return this.history;
	}

	public String getInputFileString() {
		return this.inputFileString;
	}

	public String getOutputFileString() {
		return this.outputFileString;
	}

	public boolean getResult() {
		return this.isSolvable;
	}

	public long getSetupTime() {
		return this.setupTime;
	}

	public String getSolver() {
		return this.solverId;
	}

	public State getStatus() {
		return this.state;
	}

	public long getTimeout() {
		return this.timeout;
	}

	synchronized public void handleResult(final TQbf terminatedTqbf) {
		this.handledResults++;
		if (!this.isRunning()) {
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
		if (Master.configuration.getProperty(Configuration.BENCHMARK_MODE, Boolean.class)) {
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
			LOGGER.debug("maxTimeFinished: " + maxTimeOfFinished + ", minTimeComputing: " + minTimeOfComputing);
			if (minTimeOfComputing > maxTimeOfFinished) {
				solved = this.mergeFinishedTqbfsInOrder();
			}

		} else {
			solved = this.mergeQbf(terminatedTqbf.getId(), terminatedTqbf.getResult());
			this.completingTqbf = terminatedTqbf;
			terminatedTqbf.setMerged();
		}

		LOGGER.info("Result of tqbf(" + terminatedTqbf.getId() + ") merged into Qbf of Job " + this.id + " (" + terminatedTqbf.getResult()
				+ ")");
		// this.formulaDesignations.remove(r.tqbfId);
		if (solved) {
			this.complete(this.decisionRoot.getTruthValue());
		} else {
			if (this.handledResults == this.usedCores) {
				StringBuffer errorMsg = new StringBuffer();
				errorMsg.append("No result on merge. Handled results: " + this.handledResults + ", " + "Used cores: " + this.usedCores
						+ "\n");
				errorMsg.append("Job status: " + this.getStatus() + "\n");
				for (TQbf tqbf : this.subformulas) {
					errorMsg.append("Tqbf " + tqbf.getId() + " state is " + tqbf.getState() + "\n");
				}

				// Received all subformulas but still no result...something is
				// wrong
				LOGGER.error(errorMsg.toString());
				LOGGER.error("Dumping decisiontree: \n" + this.decisionRoot.dump());
			}
		}

	}

	public boolean isComplete() {
		return (this.getStatus().equals(State.COMPLETE) ? true : false);
	}

	// public Qbf getFormula() {
	// return formula;
	// }

	public boolean isError() {
		return (this.getStatus().equals(State.ERROR) ? true : false);
	}

	public boolean isReady() {
		return (this.getStatus().equals(State.READY) ? true : false);
	}

	public boolean isRunning() {
		return (this.getStatus().equals(State.RUNNING) ? true : false);
	}

	public boolean isTimeout() {
		return (this.getStatus().equals(State.TIMEOUT) ? true : false);
	}

	public long maxSolverTime() {
		if (!this.isComplete() && !this.isTimeout()) {
			throw new IllegalStateException("Job not in state COMPLETE or TIMEOUT.");
		}

		long maxTime = 0;
		for (TQbf tqbf : this.subformulas) {
			if (tqbf.isTerminated() && tqbf.getSolverMillis() > maxTime) {
				maxTime = tqbf.getSolverMillis();
			}
		}
		return maxTime;
	}

	public double meanOverheadTime() {
		if (!this.isComplete() && !this.isTimeout()) {
			throw new IllegalStateException("Job not in state COMPLETE or TIMEOUT.");
		}

		int terminated = 0;
		long added = 0;
		for (TQbf tqbf : this.subformulas) {
			if (tqbf.isTerminated()) {
				added += tqbf.getOverheadMillis();
				terminated++;
			}
		}

		if (terminated == 0) {
			return 0;
		}
		double mean = (double) added / (double) terminated;
		return mean;
	}

	public double meanSolverTime() {
		if (!this.isComplete() && !this.isTimeout()) {
			throw new IllegalStateException("Job not in state COMPLETE or TIMEOUT.");
		}

		int terminatedCount = 0;
		long added = 0;
		for (TQbf tqbf : this.subformulas) {
			if (tqbf.isTerminated()) {
				added += tqbf.getSolverMillis();
				terminatedCount++;
			}
		}

		if (terminatedCount == 0) {
			return 0;
		}
		double mean = (double) added / (double) terminatedCount;
		return mean;
	}

	synchronized private boolean mergeFinishedTqbfsInOrder() {
		ArrayList<TQbf> finished = new ArrayList<TQbf>();
		for (TQbf tqbf : this.subformulas) {
			if (tqbf.isTerminated()) {
				finished.add(tqbf);
			}
		}

		Collections.sort(finished, new Comparator<TQbf>() {
			@Override
			public int compare(final TQbf arg0, final TQbf arg1) {
				return (int) (arg0.getComputationTime() - arg1.getComputationTime());
			}
		});

		if (finished.size() > 1) {
			assert (finished.get(0).getComputationTime() <= finished.get(1).getComputationTime());
		}

		for (TQbf tqbf : finished) {
			boolean isCompleted = this.mergeQbf(tqbf.getId(), tqbf.getResult());
			tqbf.setMerged();
			if (isCompleted) {
				this.completingTqbf = tqbf;
				return true;
			}
		}

		return false;
	}

	synchronized private boolean mergeQbf(final String tqbfId, final boolean result) {
		LOGGER.info("Merging tqbf " + tqbfId);
		if (this.decisionRoot.hasTruthValue()) {
			return true;
		}

		// find the corresponding node in the decisiontree
		DTNode tmp = this.decisionRoot.getNode(tqbfId);

		if (tmp == null) {
			// The node has been cut off from the root by another reduce()
			// The result is thus irrelevant
			return this.decisionRoot.hasTruthValue();
		}

		// set the nodes truth value
		tmp.setTruthValue(result);

		// reduce the tree
		tmp.reduce();

		// check the root for a truth value and return
		return this.decisionRoot.hasTruthValue();
	}

	private String resultText() {
		String txt;
		txt = "Job Id: " + this.id + "\n" + "State RUNNING at: " + this.history.get(State.RUNNING) + "\n" + "State COMPLETE at: "
				+ this.history.get(State.COMPLETE) + "\n" + "Total secs: " + this.totalMillis() / 1000 + "\n" + "In millis: "
				+ this.totalMillis() + "\n" + "Solver: " + this.getSolver() + "\n" + "Heuristic: " + this.heuristic.getId() + "\n"
				+ "Result: " + (this.getResult() ? "Solvable" : "Not Solvable") + "\n";

		return txt.replaceAll("\n", System.getProperty("line.separator"));
	}

	public void setFormula(final Qbf formula) {
		this.formula = formula;
	}

	public void setHistory(final HashMap<State, Date> history) {
		this.history = history;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setInputFileString(final String inputFileString) {
		this.inputFileString = inputFileString;
	}

	public void setOutputFileString(final String outputFileString) {
		this.outputFileString = outputFileString;
	}

	public void setResult(final boolean r) {
		this.isSolvable = r;
	}

	public void setSolver(final String solver) {
		this.solverId = solver;
	}

	synchronized private void setState(final State state) {
		LOGGER.debug("Job " + this.id + " to change state from " + this.state + " to " + state);
		this.state = state;
		this.history.put(state, new Date());
		this.setChanged();
		this.notifyObservers();
		this.notifyAll();
	}

	public void setTimeout(final long timeout) {
		this.timeout = timeout;
	}

	public List<TQbf> splitQbf(final Heuristic h) throws IOException {
		LOGGER.debug("Splitting into " + this.usedCores + " subformulas...");
		long start = System.currentTimeMillis();
		LOGGER.debug("Generating variable order...");
		long heuristicTime = System.currentTimeMillis();
		this.variableOrder = h.getVariableOrder(this.formula);
		heuristicTime = System.currentTimeMillis() - heuristicTime;
		LOGGER.debug("Variable order generated. Took " + heuristicTime / 1000 + " seconds.");
		LOGGER.debug("Heuristic returned variable-assignment order: " + this.variableOrder);

		int leafCtr = 1;
		ArrayDeque<DTNode> leaves = new ArrayDeque<DTNode>();
		this.decisionRoot = new DTNode(DTNode.DTNodeType.TQBF);
		leaves.addFirst(this.decisionRoot);

		// Generate the tree
		LOGGER.debug("Generating decision tree...");

		while (leafCtr < this.usedCores) {
			DTNode leaf = leaves.pollLast();
			Integer splitVar = new ArrayList<Integer>(this.variableOrder).get(leaf.getDepth());
			if (this.formula.aVars.contains(splitVar)) {
				leaf.setType(DTNode.DTNodeType.AND);
			} else if (this.formula.eVars.contains(splitVar)) {
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

		LOGGER.debug(String.format("DT-Leaves: %d, Desired Cores: %d, Variable Order size: %d", leaves.size(), this.usedCores,
				this.variableOrder.size()));

		assert (leaves.size() == this.usedCores);

		LOGGER.debug("Generating TransmissionQbfs...");
		List<TQbf> tqbfs = new ArrayList<TQbf>();

		// Generate ids for leaves and corresponding tqbfs
		int idCtr = 0;
		for (DTNode node : leaves) {
			String tqbfId = this.id + "." + Integer.toString(idCtr++);
			node.setId(tqbfId);
			TQbf tqbf = new TQbf(tqbfId, this, this.solverId, node.variablesAssignedTrue, node.variablesAssignedFalse, this.timeout,
					this.serializedFormula);
			tqbfs.add(tqbf);
		}
		assert (tqbfs.size() == this.usedCores);
		long end = System.currentTimeMillis();
		LOGGER.debug("Formula splitted. Took " + (end - start) / 1000 + " seconds.");
		// logger.debug("\n" + decisionRoot.dump());
		return tqbfs;
	}

	public void start() {
		// new TimeoutTimer(this.timeout, this);
		this.setState(State.RUNNING);
		LOGGER.info("Job started " + this.id + " ...\n" + "	Started at:  " + this.history.get(State.RUNNING) + "\n" + "	UsedCores: "
				+ this.usedCores);

		Distributor.getInstance().scheduleJob(this);
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

	synchronized public void timeout() {
		if (this.isComplete() || this.isError()) {
			return;
		}
		LOGGER.info("Timeout reached. Job: " + this.id + ", Timeout: " + this.timeout);
		this.abortComputations();
		this.setState(State.TIMEOUT);
		this.freeResources();
	}

	public long totalMillis() {
		if (!this.isTimeout() && !this.isComplete()) {
			LOGGER.error("State was expected TIMEOUT or COMPLETE. But was: " + this.getStatus());
			throw new IllegalStateException();
		}

		long total = 0;
		total += this.getSetupTime();
		if (this.isTimeout()) {
			total += this.timeout * 1000;
		} else {
			total += this.completingTqbf.getComputationTime();
		}

		return total;
	}

	/*
	 * Observes its tqbfs
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(final Observable o, final Object arg1) {
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

}
