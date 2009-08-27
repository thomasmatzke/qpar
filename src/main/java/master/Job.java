package main.java.master;

import java.util.Date;

import main.java.logic.Qbf;

public class Job {

	private Qbf formula;
	private String solver;
	private String outputFileString;
	private String inputFileString;
	private Date startedAt;
	private Date stoppedAt;
	private String id;
	
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
