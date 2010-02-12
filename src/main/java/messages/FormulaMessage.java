package main.java.messages;

import java.io.Serializable;

import main.java.logic.TransmissionQbf;

/**
 * Encapsulates a qbf and the solver specified to solve it
 * @author thomasm
 *
 */
public class FormulaMessage implements Serializable {

	private TransmissionQbf formula;
	private String solver;
	
	public TransmissionQbf getFormula() {
		return formula;
	}

	public void setFormula(TransmissionQbf formula) {
		this.formula = formula;
	}

	public void setSolver(String solver) {
		this.solver = solver;
	}

	public String getSolver() {
		return solver;
	}

}
