package main.java.master.Console;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import main.java.QPar;
import main.java.Util;
import main.java.logic.Qbf;
import main.java.logic.heuristic.Heuristic;
import main.java.logic.heuristic.HeuristicFactory;
import main.java.master.Evaluation;
import main.java.master.Job;
import main.java.master.MasterDaemon;
import main.java.master.Slave;

public class Shell implements Runnable{

	private static Logger 	logger = Logger.getLogger(MasterDaemon.class);	
	private String 			prompt 			= ">";
	private BufferedReader 	in;
	private boolean 		run				= true;
	private static int 		waitfor_count 	= 0; 		// Is increased by Slave if a Slave has the right solver
	private static int 		waitfor_cores;				// We want that many cores before proceeding
	private static String 	waitfor_solver 	= null; 	// We are waiting for Slaves with this kind of solver
	private static String 	waitfor_jobid 	= "";
	
	public Shell() {
		logger.setLevel(QPar.logLevel);
		in 	= new BufferedReader(new InputStreamReader(System.in));
	}
	
	public Shell(BufferedReader in) {
		this.in = in;
		prompt = "";
	}
		
	public static int getWaitfor_cores() {
		return waitfor_cores;
	}

	public static void setWaitfor_cores(int waitfor_cores) {
		Shell.waitfor_cores = waitfor_cores;
	}
	
	public static void setWaitfor_jobid(String waitfor_jobid) {
		Shell.waitfor_jobid = waitfor_jobid;
	}
	
	public static String getWaitfor_jobid() {
		return waitfor_jobid;
	}

	public static int getWaitfor_count() {
		return waitfor_count;
	}

	public static void setWaitfor_count(int waitfor_count) {
		Shell.waitfor_count = waitfor_count;
	}

	public static String getWaitfor_solver() {
		return waitfor_solver;
	}

	public static void setWaitfor_solver(String waitfor_solver) {
		Shell.waitfor_solver = waitfor_solver;
	}

	public void run() {	
		String line = "";
		while(run) {
			put(prompt);
			line = read();
			if(prompt.equals(""))
				puts(line);
			if(line == null)
				return;
			if(line.length() < 1) continue;
			if(line.startsWith("#")) continue;
			parseLine(line);

		}
	}

	private void parseLine(String line) {
		StringTokenizer token = new StringTokenizer(line);
		switch (Command.toCommand(token.nextToken().toUpperCase()))
		{
			case EVALUATE:
				evaluate(token);
				break;
			case NEWJOB:
				newjob(token);
				break;
			case STARTJOB:
				startjob(token);
				break;
			case ABORTJOB:
				abortjob(token);
				break;
			case VIEWJOBS:
				viewjobs();
				break;
			case VIEWSLAVES:
				viewslaves();
				break;
			case KILLSLAVE:
				killslave(token);
				break;
			case SOURCE:
				source(token);
				break;
			case WAITFORRESULT:
				waitforresult(token);
				break;
			case KILLALLSLAVES:
				killallslaves();
				break;
			case WAITFORSLAVE:
				waitforslave(token);
				break;
			case HELP:
				help();
				break;
			case QUIT:
				quit();
				break;
			case NOVALUE:
				puts("Command not found (try \"help\"");
		        break;
		    default:
		        assert(false);
		}
		
	}

	/**
	 * Syntax: EVALUATE directory_path_to_formulas cores solverId timeout [reference_file]
	 */
	private void evaluate(StringTokenizer token) {
		File	directory			= null;
		int 	cores				= 2;
		long 	timeout				= 60000;
		String 	solverId			= "qpro";
		Vector<String>	heuristics	= HeuristicFactory.getAvailableHeuristics();
		String	referenceFileName 	= "qpro_results.txt";
		
		try{
			directory 			= new File(token.nextToken());
			cores				= Integer.parseInt(token.nextToken());
			solverId			= token.nextToken();
			timeout				= Integer.parseInt(token.nextToken()) * 1000;
			if(token.hasMoreTokens())
				referenceFileName	= token.nextToken();
		} catch(NoSuchElementException e) {
			puts("Syntax: EVALUATE directory_path_to_formulas cores solverId timeout [reference_file]");
		}
		String report = "Evaluation Report\n" +
						"Solver: \t" + solverId + "\n" +
						"Timeout: \t" + timeout + "\n" +
						"Cores: \t" + cores + "\n" +
						"Directory: \t" + directory + "\n\n" +
						"cores\t";
	
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			report += String.format("%s_total\t%s_timeouts\t%s_errors\t", h, h, h);
		}
		report = report.trim() + "\n";
				
		Evaluation[][]	result	= new Evaluation[cores][heuristics.size()];
		
		// Wait for #cores
		waitforslaves(cores, solverId);

		for(int c = 2; c <= cores; c++) {
			String line = "" + c + "\t";
			for(String h : HeuristicFactory.getAvailableHeuristics()) {
				Evaluation e = new Evaluation(directory, h, solverId, timeout, c);
				result[c-2][heuristics.indexOf(h)] = e;
				e.evaluate();
				line += e.toString() + "\t";
			}
			line = line.trim();
			line += "\n";
			report += line;
		}
		
		// Check correctness
		for(File f : directory.listFiles()) {
			if(f.getName().equals("evaluation.txt") || f.getName().equals(referenceFileName))
				continue;
			Boolean compare = null;
			for(int c = 2; c <= cores; c++) {
				for(String h : HeuristicFactory.getAvailableHeuristics()) {
					Boolean current = result[c-2][heuristics.indexOf(h)].getResults().get(f);
					if(compare == null && current != null) {
						compare = current;
					} else if(compare != null && compare != current) {
						logger.error("Correctness error detected: File: " + f + ", Cores: " + c + ", Heuristic: " + h);
						MasterDaemon.bailOut();
					}
					
				}
			}			
		}
		
		
		String evalPath = directory.getAbsolutePath() + File.separator + "evaluation.txt";
		
		report = report.replaceAll("\n", System.getProperty("line.separator"));
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(evalPath));
			out.write(report);
			out.flush();
		} catch (IOException e) {
			logger.error(e);
		}
		
	}
	
	/**
	 * Syntax: KILLALLSLAVES
	 */
	private void killallslaves() {
		for(Slave s : Slave.getSlaves().values()) {
			s.kill("User request");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
	}

	/**
	 * Syntax: WAITFORRESULT jobid
	 * @param token
	 */
	private void waitforresult(StringTokenizer token) {
		
		try{
			waitfor_jobid = token.nextToken();
		} catch(NoSuchElementException e) {
			puts("Syntax: WAITFORRESULT jobid");
			return;
		}
		
		try {
			synchronized(MasterDaemon.getShellThread()) {
				MasterDaemon.getShellThread().wait();
			}
		} catch (InterruptedException e) {}
		
	}

	/**
	 * Syntax: WAITFORSLAVE number_of_cores solverid
	 * @param token
	 */
	private void waitforslave(StringTokenizer token) {
		try{
			waitforslaves(	Integer.parseInt(token.nextToken()),
							token.nextToken());
		} catch(NoSuchElementException e) {
			puts("Syntax: WAITFORSALVE number_of_cores solverid");
			return;
		}
	}
	
	/**
	 * Procedure to be used internally to wait for slaves (without the
	 * shell-parsing part)
	 * @param cores
	 * @param solverId
	 */
	private void waitforslaves(int cores, String solverId) {
		setWaitfor_solver(solverId);
		setWaitfor_cores(cores);
		
		while(true) {
			if(waitfor_count > 0 && waitfor_cores <= waitfor_count) {
				return;
			}
				
			try {
				synchronized(MasterDaemon.getShellThread()) {
					MasterDaemon.getShellThread().wait();
				}
			} catch (InterruptedException e) {}
		}
	}

	/**
	 * Syntax: SOURCE path
	 * @param token
	 */
	private void source(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(token.nextToken()));
				String line;
				while((line = in.readLine()) != null) {
					parseLine(line);
				}
			} catch (FileNotFoundException e) {
				logger.error("Error while reading formula file: " + e);
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			puts("Syntax: SOURCE path");
		}	
		
		
	}

	private void help() {
		puts("Allowed comands are NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, VIEWSLAVES, KILLSLAVE, HELP, SOURCE, WAITFORSLAVE, KILLALLSLAVES, WAITFORRESULT, QUIT (Case insensitive)");
	}

	/**
	 * Syntax: KILLSLAVE hostname
	 * @param token
	 */
	private void killslave(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			String hostname = token.nextToken();
			Slave.getSlaves().get(hostname).kill("User command");
		} else {
			puts("Syntax: KILLSLAVE hostname");
		}		
	}

	/**
	 * Syntax: VIEWJOBS
	 */
	private void viewjobs() {
		puts("JOBID\tSTARTED\tFINISHED\tSTATUS");
		for(Job j : Job.getJobs().values()) {
			puts(j.getId() + "\t" + j.getStartedAt() + "\t" + j.getStoppedAt() + "\t" + Job.getStatusDescription(j.getStatus()));
		}
	}
	
	/**
	 * Syntax: VIEWSLAVES
	 */
	private void viewslaves() {
		puts("HOSTNAME\tCORES\tCURRENT_JOBS");
		for(Slave s : Slave.getSlaves().values()) {
			puts(s.getHostName() + "\t" + s.getCores() + "\t" + Util.join(s.getCurrentJobs(), ","));
		}
	}

	/**
	 * Syntax: ABORTJOB jobid
	 * @param token
	 */
	private void abortjob(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			Job.getJobs().get(token.nextToken()).abort();
		} else {
			puts("Syntax: ABORTJOB jobid");
		}	
	}

	/**
	 * Syntax: STARTJOB jobid
	 * @param token
	 */
	private void startjob(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			try {
				Job.getJobs().get(token.nextToken()).start();
			} catch(FileNotFoundException e) {
				logger.error("Error while reading formula file: " + e);
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			puts("Syntax: STARTJOB jobid");
		}			
	}

	/**
	 * Syntax: NEWJOB path_to_formula path_to_outputfile solverid heuristic timeout
	 * @param token
	 */
	private void newjob(StringTokenizer token) {
		try{
			String 	input_path 	= token.nextToken();
			String 	output_path = token.nextToken();
			String 	solverid 	= token.nextToken();
			String 	heuristic 	= token.nextToken();
			int		maxCores	= Integer.parseInt(token.nextToken());
			Job.createJob(input_path, output_path, solverid, heuristic, 0, maxCores);
			
		} catch(NoSuchElementException e) {
			puts("Syntax: NEWJOB path_to_formula path_to_outputfile solverid heuristic max_cores");
		}
	}

	private void quit() {
		this.run = false;
		System.exit(0);
	}
	
	private void put(String s) {
		System.out.print(s);
	}
	
	private void puts(String s) {
		System.out.println(s);
	}
	
	private String read() {
		String line = null;
		try {
			line = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}
	
}
