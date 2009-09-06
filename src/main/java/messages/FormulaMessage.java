package main.java.messages;

import java.io.Serializable;

import main.java.logic.TransmissionQbf;

public class FormulaMessage implements Serializable {

	private String jobId;
	private TransmissionQbf formula;
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public TransmissionQbf getFormula() {
		return formula;
	}
	public void setFormula(TransmissionQbf formula) {
		this.formula = formula;
	}
	
}
