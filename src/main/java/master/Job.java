package main.java.master;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import main.java.QPar;
import main.java.logic.DTNode;
import main.java.logic.Qbf;
import main.java.logic.TransmissionQbf;
import main.java.logic.heuristic.Heuristic;
import main.java.logic.heuristic.HeuristicFactory;
import main.java.logic.parser.TokenMgrError;
import main.java.master.console.Shell;
import main.java.rmi.Result;
import main.java.rmi.Result.Type;
import main.java.rmi.SlaveRemote;

import org.apache.log4j.Logger;

public class Job extends Observable{

	public enum Status {
		READY, RUNNING, COMPLETE, ERROR, TIMEOUT
	}

	private static volatile int idCounter = 0;
	private static Map<String, Job> jobs = new HashMap<String, Job>();
	private static AbstractTableModel tableModel;
	static Logger logger = Logger.getLogger(Job.class);

	private boolean jobResult;
	private long timeout = 0;
	private Qbf formula;
	public DTNode decisionRoot = null;
	public byte[] serializedFormula;

	public volatile ConcurrentMap<String, SlaveRemote> formulaDesignations = new ConcurrentHashMap<String, SlaveRemote>();
	public volatile BlockingQueue<String> acknowledgedComputations = new LinkedBlockingQueue<String>();
	public volatile int startedComputations = 0;	
	public ArrayList<Long> solverTimes = new ArrayList<Long>();
	public ArrayList<Long> overheadTimes = new ArrayList<Long>();
	public String heuristic, id, inputFileString, outputFileString, solver;
	public int usedCores = 0, resultCtr = 0;
	
	public volatile Status status;
	private volatile Object statusLock = new Object(); 
	
	private List<TransmissionQbf> subformulas;
	private Date startedAt = null, solvedAt = null;

	public Job(String inputFile, String outputFile,	String solverId, String heuristicId, long timeout, int maxCores) {
//		Job job = new Job();
		this.usedCores = maxCores;
		this.setTimeout(timeout);
		this.setId(allocateJobId());
		this.setInputFileString(inputFile);
		this.setOutputFileString(outputFile);
		this.setSolver(solverId);
		this.setHeuristic(heuristicId);
		this.setStatus(Status.READY);
		addJob(this);
		logger.info("Job created. \n" + "	JobId:        " + this.id + "\n"
				+ "	HeuristicId:  " + this.getHeuristic() + "\n"
				+ "	SolverId:     " + this.getSolver() + "\n"
				+ "	#Cores:       " + this.usedCores + "\n"
				+ "	Inputfile:    " + this.getInputFileString() + "\n"
				+ "	Outputfile:   " + this.getOutputFileString() + "\n");
//		return job;
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

	public void startBlocking() {
		this.start();
		long restTimeout = this.timeout * 1000;
		
//		logger.info("rest timeout initial: " + restTimeout);
		while(true) {
			long waited = System.currentTimeMillis();
			synchronized(this) {
				if(this.status == Status.COMPLETE || this.status == Status.ERROR) {
					break;
				}
				try { this.wait(restTimeout); } catch (InterruptedException e) {} 
			}
			waited = System.currentTimeMillis() - waited;
//			logger.info("waited: " + waited);
			restTimeout -= waited;
//			logger.info("Rest timeout " + restTimeout);
			
			synchronized(this.statusLock) {
				
				if(this.status == Status.COMPLETE || this.status == Status.ERROR) {
					break;
				}
				
				if(restTimeout <= 0) {
					this.status = Status.TIMEOUT;
					logger.info("Timeout reached. Job: " + this.id + ", Timeout: " + this.timeout);
					abortComputations();
					break;
				}			
				
			}
		}
		setChanged();
        notifyObservers();
	}

	public void start() {
		this.startedAt = new Date();
		int availableCores = 0;
		this.setStatus(Status.RUNNING);
		if (tableModel != null)
			tableModel.fireTableDataChanged();
		try {
			this.formula = new Qbf(inputFileString);
		} catch (IOException e) {
			logger.error(this.inputFileString, e);
			this.setStatus(Status.ERROR);
			return;
		} catch (TokenMgrError e) {
			logger.error(this.inputFileString, e);
			this.setStatus(Status.ERROR);
			return;
		}

		ArrayList<SlaveRemote> slots = new ArrayList<SlaveRemote>();
		ArrayList<SlaveRemote> slaves;
		try {
			slaves = SlaveRegistry.instance().getSlavesWithSolver(this.solver);

			for (SlaveRemote slave : slaves) {
				int freeCores = slave.freeCores();
				availableCores += freeCores;
				for (int i = 0; i < freeCores; i++) {
					slots.add(slave);
				}
			}
		} catch (RemoteException e) {
			logger.error(e);
			this.setStatus(Status.ERROR);
			return;
		}

		logger.debug("Available Cores: " + availableCores + ", Used Cores: "
				+ usedCores);

		Collections.shuffle(slots);
		String slotStr = "";
		try {
			for (SlaveRemote s : slots)
				slotStr += s.getHostName() + " ";
		} catch (RemoteException e) {
			logger.error("RMI fail", e);
			this.setStatus(Status.ERROR);
			return;
		} catch (UnknownHostException e) {
			logger.error("Host not known...", e);
			this.setStatus(Status.ERROR);
			return;
		}

		logger.debug("Computationslots generated: " + slotStr.trim());

		try {
			this.subformulas = splitQbf();
		} catch (IOException e1) {
			logger.error("Couldnt split formula", e1);
			this.setStatus(Status.ERROR);
			return;
		}

		if (slots.size() < this.subformulas.size()) {
			logger.error("Not enough cores available for Job. Job failed.");
			this.setStatus(Status.ERROR);
			return;
		}

		logger.info("Job started " + this.id + " ...\n" + 
				"	Started at:  " + startedAt + "\n" + 
				"	Subformulas: " + this.subformulas.size() + "\n" + 
				"	Cores(avail):" + availableCores + "\n" +
				"	Cores(used): " + usedCores + "\n" + 
				"	Slaves:      " + slaves.size());

		int slotIndex = 0;
		for (TransmissionQbf sub : subformulas) {
			synchronized (this.statusLock) {
				if (this.getStatus() != Status.RUNNING)
					return;
				sub.solverId = this.solver;
				sub.jobId = this.id;
				sub.timeout = this.timeout;
				SlaveRemote s = slots.get(slotIndex);
				slotIndex += 1;

				try {
					new Thread(new TransportThread(s, sub, this.solver))
							.start();
				} catch (UnknownHostException e) {
					logger.error("Host not found", e);
				} catch (RemoteException e) {
					logger.error("RMI fail", e);
				} catch (IOException e) {
					logger.error("Something in IO failed...", e);
				}
				formulaDesignations.put(sub.id, s);
			}
		}
	}

	public List<TransmissionQbf> splitQbf() throws IOException {
		logger.debug("Splitting into " + usedCores + " subformulas...");
		long start = System.currentTimeMillis();
		Heuristic h = HeuristicFactory.getHeuristic(this.getHeuristic(), formula);
		logger.info("Generating variable order...");
		long heuristicTime = System.currentTimeMillis();
		Integer[] order = h.getVariableOrder().toArray(new Integer[0]);
		heuristicTime = System.currentTimeMillis() - heuristicTime;
		logger.info("Variable order generated. Took " + heuristicTime/1000 + " seconds.");
		logger.debug("Heuristic returned variable-assignment order: " + Arrays.toString(order));
			
		int leafCtr = 1;
		ArrayDeque<DTNode> leaves = new ArrayDeque<DTNode>();
		decisionRoot = new DTNode(DTNode.DTNodeType.TQBF);
		leaves.addFirst(decisionRoot);
		
		// Generate the tree
		logger.debug("Generating decision tree...");
		while(leafCtr < usedCores) {
			DTNode leaf 		= leaves.pollLast();
			Integer splitVar 	= order[leaf.getDepth()]; 
			if(formula.aVars.contains(splitVar)) {
				leaf.setType(DTNode.DTNodeType.AND);
			} else if(formula.eVars.contains(splitVar)) {
				leaf.setType(DTNode.DTNodeType.OR);
			}
			DTNode negChild = new DTNode(DTNode.DTNodeType.TQBF);
			negChild.variablesAssignedFalse.add(splitVar);
			negChild.setParent(leaf);
			negChild.getDepth();
			DTNode posChild = new DTNode(DTNode.DTNodeType.TQBF);
			posChild.variablesAssignedTrue.add(splitVar);
			posChild.setParent(leaf);
			posChild.getDepth();
			leaf.addChild(negChild); leaf.addChild(posChild);
			leaves.addFirst(negChild); leaves.addFirst(posChild);
			leafCtr++;
		} 
		
//logger.info("\n" + decisionRoot.dump());
		
		assert(leaves.size() == usedCores);
		
		logger.debug("Generating TransmissionQbfs...");
		List<TransmissionQbf> tqbfs = new ArrayList<TransmissionQbf>();
		
		// We only want to serialize the tree once
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		out = new ObjectOutputStream(bos);
		out.writeObject(formula.root);
		out.close();
		byte[] serializedFormula = bos.toByteArray();
		
		// Generate ids for leaves and corresponding tqbfs
		int idCtr = 0;
		for(DTNode node : leaves) {
			String id = this.id + "." + Integer.toString(idCtr++);
			node.setId(id);
			TransmissionQbf tqbf = new TransmissionQbf();
			tqbf.falseVars.addAll(node.variablesAssignedFalse);
			tqbf.trueVars.addAll(node.variablesAssignedTrue);
			tqbf.setRootNode(formula.root);
			tqbf.serializedFormula = serializedFormula;
			tqbf.id = id;
			tqbf.setEVars(formula.eVars);
			tqbf.setAVars(formula.aVars);
			Vector<Integer> tmpVars = new Vector<Integer>();
			tmpVars.addAll(formula.aVars);
			tmpVars.addAll(formula.eVars);
			tqbf.setVars(tmpVars);
			tqbfs.add(tqbf);
		}
		assert(tqbfs.size() == usedCores);
		long end = System.currentTimeMillis();
		logger.debug("Formula splitted. Took " + (end-start)/1000 + " seconds.");
		return tqbfs;
	}
	
	public boolean mergeQbf(String tqbfId, boolean result) {
		if(decisionRoot.hasTruthValue())
			return true;
		
		// find the corresponding node in the decisiontree
		DTNode tmp = decisionRoot.getNode(tqbfId);
		
		
		if(tmp == null) {
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
	
	public void abort(String why) {
		synchronized(this.statusLock) {
			if(this.status != Status.RUNNING)
				return;
			
			logger.info("Job abort. Reason: " + why);
			abortComputations();
			this.status = Status.ERROR;
			if (tableModel != null)
			tableModel.fireTableDataChanged();
			this.freeResources();
		}
	}

	private void abortComputations() {
		if(this.formulaDesignations == null)
			return;
		String tqbfId = null;
		while (this.formulaDesignations.size() > 0) {
			try {
				tqbfId = this.acknowledgedComputations.take();
			} catch (InterruptedException e) {
			}
			logger.info("Aborting Formula " + tqbfId + " ...");
			SlaveRemote designation = this.formulaDesignations.get(tqbfId);
			if (designation != null) {
				try {
					designation.abortFormula(tqbfId);
				} catch (RemoteException e) {
					logger.error("RMI fail", e);
				}
				this.formulaDesignations.remove(tqbfId);
				logger.info("Formula " + tqbfId + " aborted.");
				logger.info("Still running: " + this.formulaDesignations.keySet());
			}
		}
		logger.info("All formulas aborted.");
	}
	
	public void setResult(boolean r) {
		this.jobResult = r;
	}

	public boolean getResult() {
		return jobResult;
	}

	private void freeResources() {
		this.formula = null;
		this.formulaDesignations = null;
		this.subformulas = null;
		System.gc();
	}

	public long totalMillis() {
		// if(this.status != Job.COMPLETE)
		// return -1;
		return this.getSolvedAt().getTime() - this.getStartedAt().getTime();
	}

	public long totalSecs() {
		// if(this.status != Job.COMPLETE)
		// return -1;
		return (this.getSolvedAt().getTime() - this.getStartedAt().getTime()) / 1000;
	}

	private String resultText() {
		String txt;
		txt = "Job Id: " + this.id + "\n" + "Started at: "
				+ this.getStartedAt() + "\n" + "Stopped at: "
				+ this.getSolvedAt() + "\n" + "Total secs: " + totalSecs()
				+ "\n" + "In millis: " + totalMillis() + "\n" + "Solver: "
				+ this.getSolver() + "\n" + "Heuristic: " + this.getHeuristic()
				+ "\n" + "Result: "
				+ (this.getResult() ? "Solvable" : "Not Solvable") + "\n";

		return txt.replaceAll("\n", System.getProperty("line.separator"));
	}

	public void handleResult(Result r) {
		synchronized(this.statusLock) {
			if(this.status != Status.RUNNING)
				return;
			
			if(r.type == Result.Type.ERROR) {
				logger.error("Slave returned error for subformula: " + r.tqbfId, r.exception);
				this.status = Status.ERROR;
				this.abortComputations();
				synchronized(this) { this.notifyAll(); }
				return;
			} 
			
			resultCtr++;
			
			boolean tqbfResult = false;
			if(r.type == Type.FALSE) {
				tqbfResult = false;
			} else if (r.type == Type.TRUE){
				tqbfResult = true;
			} else {
				assert(false);
			}
			
			synchronized(this.solverTimes) {
				this.solverTimes.add(r.solverTime);
			}
			synchronized(this.overheadTimes) {
				this.overheadTimes.add(r.overheadTime);
			}

			
			boolean solved = mergeQbf(r.tqbfId, tqbfResult);
			logger.info("Result of tqbf(" + r.tqbfId + ") merged into Qbf of Job " + this.id + " (" + r.type + ")");
			this.formulaDesignations.remove(r.tqbfId);
			if (solved) {
				this.setSolvedAt(new Date());
				fireJobCompleted(decisionRoot.getTruthValue());
			} else {
				if (resultCtr == subformulas.size()) {
					// Received all subformulas but still no result...something is
					// wrong
					logger.fatal("Merging broken!");
					logger.fatal("Dumping decisiontree: \n"
							+ decisionRoot.dump());
					System.exit(-1);
				}
			}
		}	
		
	}
	
	private void fireJobCompleted(boolean result) {
		this.setStatus(Status.COMPLETE);
		this.setResult(result);
		
		synchronized(this) { this.notifyAll(); }
				
		logger.info("Job complete. Resolved to: " + result
				+ ". Aborting computations.");
		this.abortComputations();

		// Write the results to a file
		// But only if we want that. In case of a evaluation
		// the outputfile is set to null
		if (this.getOutputFileString() != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(
						this.getOutputFileString()));
				out.write(resultText());
				out.flush();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		if (Shell.getWaitfor_jobid().equals(this.id)) {
			synchronized (Master.getShellThread()) {
				Master.getShellThread().notify();
			}
		}
		if (Job.getTableModel() != null)
			Job.getTableModel().fireTableDataChanged();
		this.freeResources();
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
		return solver;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public Status getStatus() {
		return status;
	}

	public Date getSolvedAt() {
		return solvedAt;
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
		this.solver = solver;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public void setStatus(Status status) {
		this.status = status;
		if (Job.getTableModel() != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Job.getTableModel().fireTableDataChanged();
				}
			});
		}
	}

	public void setSolvedAt(Date stoppedAt) {
		// Only allow this once, in case of an erroneous second call
		if (this.solvedAt == null)
			this.solvedAt = stoppedAt;
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
		long added = 0;
		synchronized (solverTimes) {
			for (long time : solverTimes) {
				added += time;
			}
		}
		double mean = (double) added / (double) solverTimes.size();
		return mean;
	}
	
	public long maxSolverTime() {
		long maxTime = 0;
		synchronized(solverTimes) {
			maxTime = Collections.max(solverTimes);
		}
		return maxTime;
	}
	
	public double meanOverheadTime() {
		long added = 0;
		synchronized (overheadTimes) {
			for (long time : overheadTimes) {
				added += time;
			}
		}
		double mean = (double) added / (double) overheadTimes.size();
		return mean;
	}
	
	public void notifyComputationStarted(String tqbfId) {
		try {
			acknowledgedComputations.put(tqbfId);
			this.startedComputations++;
			setChanged();
	        notifyObservers();
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}
	
	/**
	* getter method for solved
	* @return TRUE if there's a result, FALSE otherwise
	*/
//	synchronized public boolean isSolved() {
//		return decisionRoot.hasTruthValue();
//	}
	
//	/**
//	* getter method for satisfiable
//	* @return TRUE the QBF is satisfiable, FALSE if not
//	*/
//	synchronized public boolean getResult() {
//		return decisionRoot.getTruthValue();
//	}

}
