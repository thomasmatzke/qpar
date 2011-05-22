package main.java.slave.solver;

import main.java.slave.tree.ReducedInterpretation;

public interface SolverPlugin extends Runnable {
	public void initialize(ReducedInterpretation ri) throws Exception;
	public void kill();
	public Boolean waitForResult() throws Exception;
}
