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
	
	public FormulaMessage(TransmissionQbf formula, String solver) {
		this.formula 	= formula;
		this.solver 	= solver;
	}

//	public void setFormula(TransmissionQbf formula) {
//		this.formula = formula;
//	}
//
//	public void setSolver(String solver) {
//		this.solver = solver;
//	}
	
	public TransmissionQbf getFormula() {
		return formula;
	}
	
	public String getSolver() {
		return solver;
	}

}
