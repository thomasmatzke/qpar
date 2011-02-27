package main.java.evaluation;

import java.util.List;

import main.java.master.Job;
import main.java.scheduling.Scheduler;

public interface EvaluationSuite {
		
	public void evaluate();
	
	public boolean isCorrect();
	
	public String getReport();
	
}
