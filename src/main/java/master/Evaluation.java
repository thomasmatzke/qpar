package main.java.master;

import java.io.File;
import java.util.HashMap;

import main.java.QPar;

import org.apache.log4j.Logger;

/**
 * Encapsulates solving all formulas in a directory with a specified heuristic
 * @author thomasm
 *
 */
public class Evaluation {

	static 	Logger 	logger = Logger.getLogger(Evaluation.class);
	private File 	directory, referenceFile;
	private String 	heuristicId, solverId, referenceFileName = "qpro_results.txt";
	private long	timeout;
	private int		cores;
	
	private int 					timeouts		= 0;
	private int 					errors			= 0;
	private long 					elapsedTotal 	= 0;
	private HashMap<File, Boolean> 	results 		= new HashMap<File, Boolean>();
	public long maxSolverTime;
	public long minSolverTime;
	public double meanSolverTime;
	public double meanMedianSolverTime;
	private double meanSolverTimeAbs;
	private double meanMedianSolverTimeAbs;
	
	public Evaluation(	File 	directory,
						String 	heuristicId,
						String 	solverId,
						long 	timeout,
						int 	cores) {
		this.directory 			= directory;
		this.heuristicId		= heuristicId;
		this.solverId			= solverId;
		this.timeout			= timeout;
		this.cores				= cores;
	}
	
	public Evaluation(	File 	directory,
						String 	heuristicId,
						String 	solverId,
						long 	timeout,
						int 	cores,
						String	referenceFileName) {
		this(directory,	heuristicId, solverId, timeout,	cores);
		this.referenceFileName = referenceFileName;
		
		this.referenceFile = new File(directory,referenceFileName);
	}
	
	public void evaluate() {
		int nonEmptyCtr = 0;
		for(File f : this.directory.listFiles()) {
			if(f.getName().equals("evaluation.txt") || f.getName().equals(referenceFileName))
				continue;
			try {
				Job job = Job.createJob(f.getAbsolutePath(), null, solverId, heuristicId, timeout, cores);
							
				job.startBlocking();
				
				if(job.getStatus() == Job.Status.COMPLETE) {
					elapsedTotal += job.totalMillis();
					results.put(f, job.getResult());
				} else if(job.getStatus() == Job.Status.TIMEOUT){ 
					elapsedTotal += timeout;
					timeouts++;
				} else if(job.getStatus() == Job.Status.ERROR) {
					elapsedTotal += timeout;
					errors++;
				} else {
					assert(false);
				}
				
				// Do the stats
				if(!job.solverTimes.isEmpty()) {
					nonEmptyCtr++;
					if(this.minSolverTime > job.minSolverTime())
						this.minSolverTime = job.minSolverTime();
					
					if(this.maxSolverTime < job.maxSolverTime())
						this.maxSolverTime = job.maxSolverTime();
					
					this.meanSolverTimeAbs += job.meanSolverTime();
					
					this.meanMedianSolverTimeAbs += job.medianSolverTime();
				}
				
			} catch(Throwable t) {
				logger.error("Evaluation.java", t);
				QPar.sendExceptionMail(t);
			}
		}
		
		this.meanSolverTime 		= this.meanSolverTimeAbs / nonEmptyCtr;
		this.meanMedianSolverTime 	= this.meanMedianSolverTimeAbs / nonEmptyCtr;
	}

	public String statisticsResultString() {
		return String.format("%d\t%d\t%f\t%f", this.minSolverTime, this.maxSolverTime, this.meanSolverTime, this.meanMedianSolverTime);
	}
	
	public String toString() {
		return String.format("%d\t%d\t%d", this.elapsedTotal, this.timeouts, this.errors);
	}
	
	public int getTimeouts() {
		return timeouts;
	}

	public void setTimeouts(int timeouts) {
		this.timeouts = timeouts;
	}

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public long getElapsedTotal() {
		return elapsedTotal;
	}

	public void setElapsedTotal(long elapsedTotal) {
		this.elapsedTotal = elapsedTotal;
	}

	public void setResults(HashMap<File, Boolean> results) {
		this.results = results;
	}

	public HashMap<File, Boolean> getResults() {
		return results;
	}
	
}
