package main.java.master;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import main.java.master.Console.Shell;

public class Evaluation {

	static 	Logger 	logger = Logger.getLogger(MasterDaemon.class);
	private File 	directory;
	private String 	heuristicId, solverId;
	private long	timeout;
	private int		cores;
	
	private int 	timeouts		= 0;
	private int 	errors			= 0;
	private long 	elapsedTotal 	= 0;
	
	public Evaluation(	File 	directory,
						String 	heuristicId,
						String 	solverId,
						long 	timeout,
						int 	cores) {
		this.directory 		= directory;
		this.heuristicId	= heuristicId;
		this.solverId		= solverId;
		this.timeout		= timeout;
		this.cores			= cores;		
	}
	
	public void evaluate() {
			
		for(File f : this.directory.listFiles()) {
			if(f.getName().equals("evaluation.txt"))
				continue;
			
			Job job = Job.createJob(f.getAbsolutePath(), null, solverId, heuristicId, timeout, cores);
						
			try {
				job.startBlocking();
								
				if(job.getStatus() == Job.Status.COMPLETE) {
					elapsedTotal += job.totalMillis();
				} else if(job.getStatus() == Job.Status.ERROR) {
					elapsedTotal += timeout;
					errors++;
				} else { 
					elapsedTotal += timeout;
					timeouts++;
				}							
			} catch(FileNotFoundException e) {
				logger.error("Error while reading formula file: " + e);
			} catch (IOException e) {
				logger.error(e);
			}			
		}
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
	
}
