package main.java.master;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import main.java.logic.Qbf;
import main.java.logic.TransmissionQbf;

public class Job {

	private Qbf formula;
	private String solver;
	private String heuristic;
	private String outputFileString;
	private String inputFileString;
	private Date startedAt;
	private Date stoppedAt;
	private String id;
	private static int idCounter = 0;
	private static Vector<Job> jobs = new Vector<Job>();
	private static AbstractTableModel tableModel;
	private String status;
	private List<TransmissionQbf> subformulas;
	
	// Maps tqbfids with to the computing slaves
	private Map<String, Slave> formulaDesignations = new HashMap<String, Slave>();
	
	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public static AbstractTableModel getTableModel() {
		return tableModel;
	}

	public static void setTableModel(AbstractTableModel tableModel) {
		Job.tableModel = tableModel;
	}

	public static String allocateJobId() {
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
		job.setStatus("Not started");
		addJob(job);
	}

	public static Vector<Job> getJobs() {
		if (jobs == null) {
			jobs = new Vector<Job>();
		}
		return jobs;
	}

	public static void addJob(Job job) {
		jobs.add(job);
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	public String getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(String heuristic) {
		this.heuristic = heuristic;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getStoppedAt() {
		return stoppedAt;
	}

	public void setStoppedAt(Date stoppedAt) {
		this.stoppedAt = stoppedAt;
	}

	public Qbf getFormula() {
		return formula;
	}

	public void setFormula(Qbf formula) {
		this.formula = formula;
	}

	public String getSolver() {
		return solver;
	}

	public void setSolver(String solver) {
		this.solver = solver;
	}

	public String getOutputFileString() {
		return outputFileString;
	}

	public void setOutputFileString(String outputFileString) {
		this.outputFileString = outputFileString;
	}

	public String getInputFileString() {
		return inputFileString;
	}

	public void setInputFileString(String inputFileString) {
		this.inputFileString = inputFileString;
	}

	public void start() {
		this.startedAt 		= new Date();
		this.formula 		= new Qbf(inputFileString);
		int availableCores 	= Slave.getCoresForSolver(this.solver);
		this.subformulas 	= formula.splitQbf(availableCores);
		List<Slave> slaves 	= Slave.getSlavesForSolver(this.solver);
		
		for(int i = 0; i < subformulas.size(); i++) {
			Slave designatedSlave = slaves.get(i % slaves.size());
			subformulas.get(i).setStatus("computing");
			designatedSlave.computeFormula(subformulas.get(i), this.getId());
			formulaDesignations.put(subformulas.get(i).getId(), designatedSlave);
		}
		
		tableModel.fireTableDataChanged();
	}

	public void abort() {
		for(Map.Entry<String, Slave> entry : this.formulaDesignations.entrySet()) {
			Slave s 		= entry.getValue();
			String tqbfId 	= entry.getKey();
			s.abortFormulaComputation(tqbfId);
		}
		
		tableModel.fireTableDataChanged();
	}

}
