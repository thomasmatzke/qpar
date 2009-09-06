package main.java.master;

import java.util.Date;
import java.util.Vector;

import main.java.logic.Qbf;

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
	private static Vector<Job> jobs;

	public static String allocateJobId() {
		idCounter++;
		return new Integer(idCounter).toString();
	}

	public static void createJob(String inputFile, String outputFile,
			String solverId, String heuristicId) {
		Job job = new Job();
		job.setInputFileString(inputFile);
		job.setOutputFileString(outputFile);
		job.setSolver(solverId);
		job.setHeuristic(heuristicId);
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
		for (int i = 0; i <= 3; i++) {
			MasterDaemon.getJobsModel().fireTableCellUpdated(jobs.size(), i);
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

}
