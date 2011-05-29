package qpar.slave.solver;

import qpar.slave.tree.ReducedInterpretation;

public interface SolverPlugin extends Runnable {
	public void initialize(ReducedInterpretation ri) throws Exception;
	public void kill();
	public Boolean waitForResult() throws Exception;
}
