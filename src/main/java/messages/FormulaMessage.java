package main.java.messages;

import java.io.Serializable;

import main.java.logic.TransmissionQbf;

public class FormulaMessage implements Serializable {

	private TransmissionQbf formula;
	
	public TransmissionQbf getFormula() {
		return formula;
	}

	public void setFormula(TransmissionQbf formula) {
		this.formula = formula;
	}

}
