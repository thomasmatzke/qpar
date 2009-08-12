package main.java.messages;

import java.io.Serializable;

import main.java.logic.Qbf;

public class FormulaMessage implements Serializable {

	private String jobId;
	private Qbf formula;
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public Qbf getFormula() {
		return formula;
	}
	public void setFormula(Qbf formula) {
		this.formula = formula;
	}
	
}
