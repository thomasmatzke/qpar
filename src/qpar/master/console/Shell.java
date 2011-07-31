package qpar.master.console;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;


import org.apache.log4j.Logger;

import qpar.common.Configuration;
import qpar.common.rmi.SlaveRemote;
import qpar.master.Evaluation;
import qpar.master.Job;
import qpar.master.Mailer;
import qpar.master.SlaveRegistry;
import qpar.master.heuristic.HeuristicFactory;

public class Shell implements Runnable, Observer{

	private static Logger 	logger = Logger.getLogger(Shell.class);	
	private String 			prompt 			= ">";
	private BufferedReader 	in;
	private boolean 		run				= true;
		
	public Shell() {
		in 	= new BufferedReader(new InputStreamReader(System.in));
	}
	
	public Shell(BufferedReader in) {
		this.in = in;
		prompt = "";
	}

	public void run() {	
		String line = "";
		while(run) {
			try {
				put(prompt);
				line = read();
				if(prompt.equals(""))
					puts(line);
				if(line == null)
					return;
				if(line.length() < 1) continue;
				if(line.startsWith("#")) continue;
				parseLine(line);
			} catch(Throwable t) {
				logger.error("", t);
				Configuration.sendExceptionMail(t);
			}	
		}
		
	}

	private void parseLine(String line) {
		StringTokenizer token = new StringTokenizer(line);
		switch (Command.toCommand(token.nextToken().toUpperCase()))
		{
			case EVALUATE:
				evaluate(token);
				break;
			case MAIL:
				mail(token);
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
			case KILLALLSLAVES:
				killallslaves();
				break;
			case SHUTDOWNALLSLAVES:
				shutdownallslaves();
				break;
//			case WAITFORSLAVE:
//				waitforslave(token);
//				break;
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
	
	private void evaluate(StringTokenizer token) {
		String	directory				= null;
		String	solver					= null;
		int 	cores_min				= 1;
		int 	cores_max				= 1;
		long 	timeout					= 60;
		
		try {
			directory 			= token.nextToken();
			cores_min			= Integer.parseInt(token.nextToken());
			cores_max			= Integer.parseInt(token.nextToken());
			solver 				= token.nextToken();
			timeout				= Integer.parseInt(token.nextToken());
		} catch(NoSuchElementException e) {
			puts("Syntax: PARLOGEVAL directory_path_to_formulas coresMin coresMax solver timeout");
			return;
		}
//		List<String> heuristics = new ArrayList<String>();
//		heuristics.add("simple");
//		heuristics.add("rand");
//		heuristics.add("litcount");
//		heuristics.add("probnet");
		
		Evaluation eval = new Evaluation(new File(directory), cores_min, cores_max, solver, HeuristicFactory.getAvailableHeuristics(), timeout);
		eval.evaluate();
		
		String report = eval.getReport();
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(directory).getAbsolutePath() + File.separator + "evaluation.txt"));
			out.write(report);
			out.flush();
			out.close();
		} catch (IOException e) {
			logger.error("While writing report: ", e);
		}
		
		if(Configuration.isMailEvaluationResults())
			Mailer.send_mail(Configuration.getEvaluationAddress(), Configuration.getMailServer(), Configuration.getMailUser(), Configuration.getMailPass(), "ParLogEval Report", report);
	}

	/**
	 * Syntax: MAIL_EVALUATION_REPORT my@email.com 
	 * @param token
	 */
	private void set_report_address(StringTokenizer token) {
		try {
			Mailer.email 	= token.nextToken();
			Mailer.server 	= token.nextToken();
			Mailer.user 	= token.nextToken();
			Mailer.pass 	= token.nextToken();
		} catch(NoSuchElementException e) {
			puts("Syntax: MAIL_EVALUATION_REPORT my@email.com");
		}
	}

	/**
	 * Syntax: SHUTDOWNALLSLAVES
	 */
	private void shutdownallslaves() {
		for(SlaveRemote s : SlaveRegistry.instance().getSlaves().values()) {
			try {
				s.shutdown();
			} catch (RemoteException e) {
				logger.error("RMI fail", e);
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}		
	}

	/**
	 * Sends a message to an email-address
	 * Syntax: MAIL my@email.com esmtp.server.com user pass subject message
	 * @param token
	 */
	private void mail(StringTokenizer token) {
		String email 	= null;
		String server 	= null;
		String user		= null;
		String pass		= null;
		String subject	= null;
		String message	= null;
		
		try {
			email 	= token.nextToken();
			server 	= token.nextToken();
			user	= token.nextToken();
			pass	= token.nextToken();
			subject	= token.nextToken();
			message	= token.nextToken("\n");
		} catch(NoSuchElementException e) {
			puts("Syntax: MAIL my@email.com esmtp.server.com user pass subject message");
			return;
		}
		Mailer.send_mail(email, server, user, pass, subject, message);
		
	}
		
	/**
	 * Syntax: KILLALLSLAVES
	 */
	private void killallslaves() {
		for(SlaveRemote s : SlaveRegistry.instance().getSlaves().values()) {
			try {
				s.kill("User request");
			} catch (RemoteException e) {
				logger.error("RMI fail", e);
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
	}

//	/**
//	 * Syntax: WAITFORRESULT jobid
//	 * @param token
//	 */
//	private void waitforresult(StringTokenizer token) {
//		
//		Job job = Job.getJobs().get(waitfor_jobid);
//		
//		try{
//			waitfor_jobid = token.nextToken();
//			
//			if(job == null) {
//				logger.warn("No job with id " + waitfor_jobid);
//				return;
//			}
//			if(job.getStatus() == Job.Status.ERROR)
//				return;
//			
//		} catch(NoSuchElementException e) {
//			puts("Syntax: WAITFORRESULT jobid");
//			return;
//		}
//		
//		while(job.getStatus() == Job.Status.RUNNING) {
//			synchronized(this) {
//				try {wait();} catch (InterruptedException e) {}
//			}
//		}
//		
//	}

//	/**
//	 * Syntax: WAITFORSLAVE number_of_cores solverid
//	 * @param token
//	 */
//	private void waitforslave(StringTokenizer token) {
//		try{
//			int cores 		= Integer.parseInt(token.nextToken());
//			String solver 	= token.nextToken();
//						
//			SlaveRegistry.instance().waitForCoresWithSolver(cores, solver);
//		} catch(NoSuchElementException e) {
//			puts("Syntax: WAITFORSALVE number_of_cores solverid");
//			return;
//		}
//	}
	
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
				in.close();
			} catch (FileNotFoundException e) {
				logger.error("Cant find formula file: ", e);
			} catch (IOException e) {
				logger.error("Error while reading file", e);
			}
		} else {
			puts("Syntax: SOURCE path");
		}	
				
	}

	private void help() {
		puts("Allowed comands are NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, VIEWSLAVES, KILLSLAVE, HELP, SOURCE, WAITFORSLAVE, KILLALLSLAVES, WAITFORRESULT, EMAIL, QUIT (Case insensitive)");
	}

	/**
	 * Syntax: KILLSLAVE hostname
	 * @param token
	 */
	private void killslave(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			String hostname = token.nextToken();
			try {
				SlaveRegistry.instance().getSlaves().get(hostname).kill("User command");
			} catch (RemoteException e) {
				logger.error("RMI fail", e);
			}
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
			puts(j.id + "\t" + j.getHistory().get(Job.State.RUNNING) + "\t" + j.getHistory().get(Job.State.COMPLETE) + "\t" + j.getStatus());
		}
	}
	
	/**
	 * Syntax: VIEWSLAVES
	 */
	private void viewslaves() {
		puts("HOSTNAME\tCORES");
		for(SlaveRemote s : SlaveRegistry.instance().getSlaves().values()) {
			try {
				puts(s.getHostName() + "\t" + s.getCores());
			} catch (RemoteException e) {
				logger.error("RMI fail", e);
			} catch (UnknownHostException e) {
				logger.error("Cant find hostname", e);
			}
		}
	}

	/**
	 * Syntax: ABORTJOB jobid
	 * @param token
	 */
	private void abortjob(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			Job.getJobs().get(token.nextToken()).abort("User request.");
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
			Job j = Job.getJobs().get(token.nextToken());
			j.startBlocking();
		} else {
			puts("Syntax: STARTJOB jobid");
		}			
	}

	/**
	 * Syntax: NEWJOB path_to_formula path_to_outputfile solverid heuristic cores timeout
	 * @param token
	 */
	private void newjob(StringTokenizer token) {
		try{
			String 	input_path 	= token.nextToken();
			String 	output_path = token.nextToken();
			String 	solverid 	= token.nextToken();
			String 	heuristic 	= token.nextToken();
			int		maxCores	= Integer.parseInt(token.nextToken());
			long	timeout		= Long.parseLong(token.nextToken());
			new Job(input_path, output_path, solverid, heuristic, timeout, maxCores);
			
		} catch(NoSuchElementException e) {
			puts("Syntax: NEWJOB path_to_formula path_to_outputfile solverid heuristic max_cores timeout");
		} catch (RemoteException e) {
			logger.error("", e);
		}
	}

	private void quit() {
		this.run = false;
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

	@Override
	public void update(Observable o, Object arg) {
		synchronized(this) {
			notifyAll();
		}
	}
	
}
