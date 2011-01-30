package main.java.master;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
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
			} catch(Throwable t) {
				StringWriter result = new StringWriter();
				PrintWriter writer = new PrintWriter(result);
				t.printStackTrace(writer);
				logger.error(result);
				if(QPar.isMailInfoComplete() && QPar.exceptionNotifierAddress != null)
					Mailer.send_mail(QPar.exceptionNotifierAddress, QPar.mailServer, QPar.mailUser, QPar.mailPass, "Exception Notification (Evaluation.evaluate())", result.toString());
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

	public void setResults(HashMap<File, Boolean> results) {
		this.results = results;
	}

	public HashMap<File, Boolean> getResults() {
		return results;
	}
	
}
