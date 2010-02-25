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

import main.java.logic.HeuristicFactory;
import main.java.logic.Qbf;
import main.java.logic.TransmissionQbf;

public class Job {

	public static final int READY = 0;
	public static final int RUNNING = 1;
	public static final int COMPLETE = 2;
	public static final int ERROR = 3;

	private static int idCounter = 0;
	private static Map<String, Job> jobs = new HashMap<String, Job>();
	private static AbstractTableModel tableModel;
	static Logger logger = Logger.getLogger(MasterDaemon.class);
	{
		logger.setLevel(Level.INFO);
	}

	private boolean result;

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

	public static void createJob(String inputFile, String outputFile,
			String solverId, String heuristicId) {
		Job job = new Job();
		job.setId(allocateJobId());
		job.setInputFileString(inputFile);
		job.setOutputFileString(outputFile);
		job.setSolver(solverId);
		job.setHeuristic(heuristicId);
		job.setStatus(Job.READY);
		addJob(job);
		logger.info("Job created. Id: " + job.getId() + ",\n" + "HeuristicId: "
				+ job.getHeuristic() + ",\n" + "SolverId: " + job.getSolver()
				+ ",\n" + "Inputfile: " + job.getInputFileString() + ",\n"
				+ "Outputfile: " + job.getOutputFileString() + "\n");
	}

	public static Map<String, Job> getJobs() {
		if (jobs == null) {
			jobs = new HashMap<String, Job>();
		}
		return jobs;
	}

	public static AbstractTableModel getTableModel() {
		return tableModel;
	}

	public static void setTableModel(AbstractTableModel tableModel) {
		Job.tableModel = tableModel;
	}

	private Qbf formula;
	// Maps tqbfids with to the computing slaves
	private Map<String, Slave> formulaDesignations = new HashMap<String, Slave>();

	public Map<String, Slave> getFormulaDesignations() {
		return formulaDesignations;
	}

	public void setFormulaDesignations(Map<String, Slave> formulaDesignations) {
		this.formulaDesignations = formulaDesignations;
	}

	private String heuristic;
	private String id;

	private String inputFileString;

	private String outputFileString;

	private String solver;

	private Date startedAt;

	private int status;

	private Date stoppedAt;

	private List<TransmissionQbf> subformulas;

	public void abort() {
		if (this.status != Job.RUNNING)
			return;
		logger.info("Aborting Job " + this.id + "...\n");
		logger
				.info("Aborting Formulas. Sending AbortFormulaMessages to corresponding slaves...");
		abortComputations();
		this.status = Job.ERROR;
		if (tableModel != null)
			tableModel.fireTableDataChanged();
		logger.info("AbortMessages sent.");
	}

	public void abortComputations() {
		for (Map.Entry<String, Slave> entry : this.formulaDesignations
				.entrySet()) {
			Slave s = entry.getValue();
			String tqbfId = entry.getKey();
			s.abortFormulaComputation(tqbfId);
		}
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

	public int getStatus() {
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

	public void setStatus(int status) {
		this.status = status;
	}

	public void setStoppedAt(Date stoppedAt) {
		this.stoppedAt = stoppedAt;
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
		this.subformulas = formula.splitQbf(availableCores, HeuristicFactory
				.getHeuristic(this.getHeuristic()));
		List<Slave> slaves = Slave.getSlavesWithSolver(this.solver);

		int j = 0;
		outerLoop: for (Slave slave : slaves) {
			for (int i = 0; i < slave.getCores(); i++) {
				slave.computeFormula(subformulas.get(j), this);
				formulaDesignations.put(subformulas.get(j).getId(), slave);
				j++;
				if (formulaDesignations.size() == subformulas.size())
					break outerLoop;
			}
		}

		logger.info("Job started. Splitted into " + this.subformulas.size()
				+ " subformulas, " + "with " + availableCores
				+ " available Cores on " + slaves.size() + " slaves.");
		this.status = Job.RUNNING;
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
		this.abortComputations();
		this.setStatus(Job.COMPLETE);
		this.setResult(result);
		this.setStoppedAt(new Date());
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(this.getOutputFileString()));
			out.write(resultText());
		} catch (IOException e) {
			logger.error(e);
		}
		
		if (Job.getTableModel() != null)
			Job.getTableModel().fireTableDataChanged();
	}

	private String resultText() {
		long diff = this.getStoppedAt().getTime() - this.getStartedAt().getTime();
		String txt;
		txt = 	"Job Id: " + this.getId() + "\n" +
				"Started at: " + this.getStartedAt() + "\n" +
				"Stopped at: " + this.getStoppedAt() + "\n" +
				"Total secs: " + diff / 1000 + "\n" + 
				"Solver: " + this.getSolver() + "\n" + 
				"Heuristic: " + this.getHeuristic();
				
		return txt.replaceAll("\n", System.getProperty("line.separator"));
	}
	
}
