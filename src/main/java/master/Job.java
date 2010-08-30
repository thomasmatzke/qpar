package main.java.master;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import main.java.QPar;
import main.java.logic.Qbf;
import main.java.logic.TransmissionQbf;
import main.java.logic.heuristic.HeuristicFactory;
import main.java.master.Console.Shell;

public class Job {

	public enum Status { READY, RUNNING, COMPLETE, ERROR, TIMEOUT }
	
//	public static final int READY = 0;
//	public static final int RUNNING = 1;
//	public static final int COMPLETE = 2;
//	public static final int ERROR = 3;
//	public static final int TIMEOUT = 4;

	private static int idCounter = 0;
	private static Map<String, Job> jobs = new HashMap<String, Job>();
	private static AbstractTableModel tableModel;
	static Logger logger = Logger.getLogger(MasterDaemon.class);

	private boolean result;
	private long timeout = 0;
	private Qbf formula;
	
	private Object formulaDesignations_lock = new Object();
	private volatile Map<String, Slave> formulaDesignations = new HashMap<String, Slave>(); // Maps
																					// tqbfids
																					// to
																					// the
																					// computing
																					// slaves
	private String heuristic, id, inputFileString, outputFileString, solver;
	private int maxCores = 0;
	private Status status;
	private List<TransmissionQbf> subformulas;
	private Date startedAt, stoppedAt;
	
	public Job() {
		logger.setLevel(QPar.logLevel);
	}

	private static void addJob(Job job) {
		jobs.put(job.id, job);
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
		logger.info("Job added. JobId: " + job.id);
	}

	private static String allocateJobId() {
		idCounter++;
		return new Integer(idCounter).toString();
	}

	public static Job createJob(String inputFile, String outputFile,
			String solverId, String heuristicId, long timeout, int maxCores) {
		Job job = new Job();
		job.maxCores = maxCores;
		job.setTimeout(timeout);
		job.setId(allocateJobId());
		job.setInputFileString(inputFile);
		job.setOutputFileString(outputFile);
		job.setSolver(solverId);
		job.setHeuristic(heuristicId);
		job.setStatus(Status.READY);
		addJob(job);
		logger.info("Job created. \n" +
					"	JobId:        " + job.getId() + "\n" + 
					"	HeuristicId:  "	+ job.getHeuristic() + "\n" + 
					"	SolverId:     " + job.getSolver() + "\n" +
					"	Inputfile:    " + job.getInputFileString() + "\n" + 
					"	Outputfile:   " + job.getOutputFileString() + "\n");
		return job;
	}

	public static Map<String, Job> getJobs() {
		if (jobs == null) {
			jobs = new HashMap<String, Job>();
		}
		return jobs;
	}

	public void abort() {
		if (this.status != Status.RUNNING)
			return;
		logger.info("Aborting Job " + this.id + "...\n");
		logger.info("Aborting Formulas. Sending AbortFormulaMessages to slaves...");
		abortComputations();
		this.status = Status.ERROR;
		if (tableModel != null)
			tableModel.fireTableDataChanged();
		logger.info("AbortMessages sent.");
	}

	private void abortComputations() {
		synchronized(formulaDesignations_lock) {
			for (Map.Entry<String, Slave> entry : this.formulaDesignations
					.entrySet()) {
				Slave s = entry.getValue();
				String tqbfId = entry.getKey();
				s.abortFormulaComputation(tqbfId);
			}
		}
	}

	public synchronized void startBlocking() throws IOException {
		this.start();
		while(this.getStatus() == Status.RUNNING) {
			try {
				Thread.sleep(500);
				if((startedAt.getTime() + timeout) < new Date().getTime()) {
					logger.info("Timeout reached. Aborting Job. \n" +
								"	Job Id:         " + this.id + "\n" +
								"	Timeout (secs): " + timeout/1000 + "\n");
					this.abort();
					this.status = Status.TIMEOUT;
					break;
				}
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
	}

	public void start() throws IOException {
		logger.info("Starting Job " + this.id + "...\n");
		this.startedAt = new Date();
		// try {
		this.formula = new Qbf(inputFileString);
		// } catch (IOException e) {
		// logger.error("Error while reading formula file: " + e);
		// }
		int availableCores = Slave.getCoresForSolver(this.solver);

		if(this.maxCores != 0 && availableCores > this.maxCores)
			availableCores = maxCores;
		
		this.subformulas = formula.splitQbf(availableCores, 
											HeuristicFactory.getHeuristic(this.getHeuristic(), this.formula));
		List<Slave> slaves = Slave.getSlavesWithSolver(this.solver);

		logger.info("Starting Job. \n" +
					"	# Subformulas: " + this.subformulas.size()+ "\n" + 
					"	# Cores:       " + availableCores + "\n" +
					"	# Slaves:      " + slaves.size());
		
		
		int j = 0;
		synchronized(formulaDesignations_lock) {
			outerLoop: for (Slave slave : slaves) {
				for (int i = 0; i < slave.getCores(); i++) {
					slave.computeFormula(subformulas.get(j), this);
					formulaDesignations.put(subformulas.get(j).getId(), slave);
					j++;
					// TODO: Race condition here, formulaDesignations is beeing modified
					if (formulaDesignations.size() == subformulas.size())
						break outerLoop;
				}
			}
		}
		
		this.status = Status.RUNNING;
		if (tableModel != null)
			tableModel.fireTableDataChanged();
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public boolean getResult() {
		return result;
	}

	public void fireJobCompleted(boolean result) {
		logger.info("Job complete. Resolved to: " + result + ". Aborting computations.");
		this.setStatus(Status.COMPLETE);
		this.abortComputations();
		this.setResult(result);
		this.setStoppedAt(new Date());

		// Write the results to a file
		// But only if we want that. In case of a evaluation
		// the outputfile is set to null
		if (this.getOutputFileString() != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(this
						.getOutputFileString()));
				out.write(resultText());
				out.flush();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		if (Shell.getWaitfor_jobid().equals(this.getId())) {
			synchronized (MasterDaemon.getShellThread()) {
				MasterDaemon.getShellThread().notify();
			}
		}
		if (Job.getTableModel() != null)
			Job.getTableModel().fireTableDataChanged();
		this.freeResources();
	}

	private void freeResources() {
		this.formula				= null;
		this.formulaDesignations	= null;
		this.subformulas			= null;
		System.gc();
	}
	
	public long totalMillis() {
		// if(this.status != Job.COMPLETE)
		// return -1;
		return this.getStoppedAt().getTime() - this.getStartedAt().getTime();
	}

	public long totalSecs() {
		// if(this.status != Job.COMPLETE)
		// return -1;
		return (this.getStoppedAt().getTime() - this.getStartedAt().getTime()) / 1000;
	}

	private String resultText() {
		String txt;
		txt = "Job Id: " + this.getId() + "\n" + "Started at: "
				+ this.getStartedAt() + "\n" + "Stopped at: "
				+ this.getStoppedAt() + "\n" + "Total secs: " + totalSecs()
				+ "\n" + "In millis: " + totalMillis() + "\n" + "Solver: "
				+ this.getSolver() + "\n" + "Heuristic: " + this.getHeuristic()
				+ "\n" + "Result: "
				+ (this.getResult() ? "Solvable" : "Not Solvable") + "\n";

		return txt.replaceAll("\n", System.getProperty("line.separator"));
	}

	public void handleResult(String tqbfId, boolean result) {
		if(this.status == Status.COMPLETE) {
			return;
		}
		boolean solved = formula.mergeQbf(tqbfId, result);
		logger.info("Result of tqbf(" + tqbfId + ") merged into Qbf of Job "
				+ getId());
		synchronized(formulaDesignations_lock) {
			this.formulaDesignations.remove(tqbfId);
		}
		if (solved)
			fireJobCompleted(formula.getResult());
	}

	public Qbf getFormula() {
		return formula;
	}

	public String getHeuristic() {
		return heuristic;
	}

	public String getId() {
		return id;
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
		if((this.status == Status.RUNNING) && (startedAt.getTime() + timeout) < new Date().getTime()) {
			this.abort();
			this.status = Status.TIMEOUT;
		}
		return status;
	}

	public Date getStoppedAt() {
		return stoppedAt;
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
	}

	public void setStoppedAt(Date stoppedAt) {
		this.stoppedAt = stoppedAt;
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

	public static String getStatusDescription(Status status) {
		switch (status) {
			case READY:
				return "Ready";
			case RUNNING:
				return "Running";
			case COMPLETE:
				return "Complete";
			case ERROR:
				return "Error";
			default:
				return "undefined";
		}
	}

	public Map<String, Slave> getFormulaDesignations() {
		synchronized(formulaDesignations_lock) {
			return formulaDesignations;
		}
	}

	public void setFormulaDesignations(Map<String, Slave> formulaDesignations) {
		synchronized(formulaDesignations_lock){
			this.formulaDesignations = formulaDesignations;
		}
	}
}
