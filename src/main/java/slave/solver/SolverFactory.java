package main.java.slave.solver;


public class SolverFactory {
	
	public Solver getToolByName(String name) {
		if(name == "qpro") {
			return new QProSolver();
		} // TODO: Add code for more tools here
		return null;
	}
	
}
