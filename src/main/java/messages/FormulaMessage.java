package main.java.messages;

import java.io.Serializable;

import main.java.logic.Qbf;

public class FormulaMessage implements Serializable {

	private int jobId;
	private Qbf formula;
	
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public Qbf getFormula() {
		return formula;
	}
	public void setFormula(Qbf formula) {
		this.formula = formula;
	}
	
}
