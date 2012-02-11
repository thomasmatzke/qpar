/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.Configuration;
import qpar.common.dom.heuristic.Heuristic;
import qpar.common.rmi.SlaveRemote;
import qpar.master.Evaluation;
import qpar.master.Job;
import qpar.master.Mailer;
import qpar.master.Master;
import qpar.master.SlaveRegistry;
import qpar.master.heuristic.HeuristicFactory;

public class Shell implements Runnable, Observer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Shell.class);
	private String prompt = ">";
	private final BufferedReader in;
	private boolean run = true;

	public Shell() {
		this.in = new BufferedReader(new InputStreamReader(System.in));
	}

	public Shell(final BufferedReader in) {
		this.in = in;
		this.prompt = "";
	}

	@Override
	public void run() {
		try {
			String line = "";
			while (this.run) {

				this.put(this.prompt);
				line = this.read();
				if (this.prompt.equals("")) {
					this.puts(line);
				}
				if (line == null) {
					return;
				}
				if (line.length() < 1) {
					continue;
				}
				if (line.startsWith("#")) {
					continue;
				}
				this.parseLine(line);

			}
			this.in.close();
		} catch (Throwable t) {
			LOGGER.error("", t);
			Mailer.sendExceptionMail(t);
		}

	}

	private void parseLine(final String line) {
		StringTokenizer token = new StringTokenizer(line);
		switch (Command.toCommand(token.nextToken().toUpperCase())) {
		case EVALUATE:
			this.evaluate(token);
			break;
		case MAIL:
			this.mail(token);
			break;
		case NEWJOB:
			this.newjob(token);
			break;
		case STARTJOB:
			this.startjob(token);
			break;
		case ABORTJOB:
			this.abortjob(token);
			break;
		case VIEWJOBS:
			this.viewjobs();
			break;
		case VIEWSLAVES:
			this.viewslaves();
			break;
		case KILLSLAVE:
			this.killslave(token);
			break;
		case SOURCE:
			this.source(token);
			break;
		case KILLALLSLAVES:
			this.killallslaves();
			break;
		case HELP:
			this.help();
			break;
		case QUIT:
			this.quit();
			break;
		case NOVALUE:
			this.puts("Command not found (try \"help\"");
			break;
		default:
			assert (false);
		}

	}

	private void evaluate(final StringTokenizer token) {
		String directory = null;
		String solver = null;
		int cores_min = 1;
		int cores_max = 1;
		long timeout = 60;

		try {
			directory = token.nextToken();
			cores_min = Integer.parseInt(token.nextToken());
			cores_max = Integer.parseInt(token.nextToken());
			solver = token.nextToken();
			timeout = Integer.parseInt(token.nextToken());
		} catch (NoSuchElementException e) {
			this.puts("Syntax: PARLOGEVAL directory_path_to_formulas coresMin coresMax solver timeout");
			return;
		}

		Evaluation eval = new Evaluation(new File(directory), cores_min, cores_max, solver, new ArrayList<String>(
				HeuristicFactory.getAvailableHeuristics()), timeout);
		eval.evaluate();

		String report = eval.getReport();

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(directory).getAbsolutePath() + File.separator
					+ "evaluation.txt"));
			out.write(report);
			out.flush();
			out.close();
		} catch (IOException e) {
			LOGGER.error("While writing report: ", e);
		}

		if (Master.configuration.getProperty(Configuration.MAIL_EVAL_RESULTS, Boolean.class)) {
			Mailer.send(Master.configuration.getProperty(Configuration.EVAL_RESULT_ADDRESS, String.class),
					Master.configuration.getProperty(Configuration.MAIL_SRV, String.class),
					Master.configuration.getProperty(Configuration.MAIL_USR, String.class),
					Master.configuration.getProperty(Configuration.MAIL_PW, String.class), "QPar Evaluation Report", report);
		}

	}

	/**
	 * Sends a message to an email-address Syntax: MAIL my@email.com
	 * esmtp.server.com user pass subject message
	 * 
	 * @param token
	 */
	private void mail(final StringTokenizer token) {
		String email = null;
		String server = null;
		String user = null;
		String pass = null;
		String subject = null;
		String message = null;

		try {
			email = token.nextToken();
			server = token.nextToken();
			user = token.nextToken();
			pass = token.nextToken();
			subject = token.nextToken();
			message = token.nextToken("\n");
		} catch (NoSuchElementException e) {
			this.puts("Syntax: MAIL my@email.com esmtp.server.com user pass subject message");
			return;
		}
		Mailer.send(email, server, user, pass, subject, message);

	}

	/**
	 * Syntax: KILLALLSLAVES
	 */
	private void killallslaves() {
		for (SlaveRemote s : SlaveRegistry.getInstance().getSlaves().values()) {
			try {
				s.kill("User request");
			} catch (RemoteException e) {
				LOGGER.error("RMI fail", e);
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Syntax: SOURCE path
	 * 
	 * @param token
	 */
	private void source(final StringTokenizer token) {
		if (token.hasMoreTokens()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(token.nextToken()));
				String line;
				while ((line = in.readLine()) != null) {
					this.parseLine(line);
				}
				in.close();
			} catch (FileNotFoundException e) {
				LOGGER.error("Cant find formula file: ", e);
			} catch (IOException e) {
				LOGGER.error("Error while reading file", e);
			}
		} else {
			this.puts("Syntax: SOURCE path");
		}

	}

	private void help() {
		this.puts("Allowed comands are NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, VIEWSLAVES, KILLSLAVE, HELP, SOURCE, WAITFORSLAVE, KILLALLSLAVES, EMAIL, QUIT (Case insensitive)");
	}

	/**
	 * Syntax: KILLSLAVE hostname
	 * 
	 * @param token
	 */
	private void killslave(final StringTokenizer token) {
		if (token.hasMoreTokens()) {
			String hostname = token.nextToken();
			try {
				SlaveRegistry.getInstance().getSlaves().get(hostname).kill("User command");
			} catch (RemoteException e) {
				LOGGER.error("RMI fail", e);
			}
		} else {
			this.puts("Syntax: KILLSLAVE hostname");
		}
	}

	/**
	 * Syntax: VIEWJOBS
	 */
	private void viewjobs() {
		this.puts("JOBID\tSTARTED\tFINISHED\tSTATUS");
		for (Job j : Job.getJobs().values()) {
			this.puts(j.id + "\t" + j.getHistory().get(Job.State.RUNNING) + "\t" + j.getHistory().get(Job.State.COMPLETE) + "\t"
					+ j.getStatus());
		}
	}

	/**
	 * Syntax: VIEWSLAVES
	 */
	private void viewslaves() {
		this.puts("HOSTNAME\tCORES");
		for (SlaveRemote s : SlaveRegistry.getInstance().getSlaves().values()) {
			try {
				this.puts(s.getHostName() + "\t" + s.getCores());
			} catch (RemoteException e) {
				LOGGER.error("RMI fail", e);
			} catch (UnknownHostException e) {
				LOGGER.error("Cant find hostname", e);
			}
		}
	}

	/**
	 * Syntax: ABORTJOB jobid
	 * 
	 * @param token
	 */
	private void abortjob(final StringTokenizer token) {
		if (token.hasMoreTokens()) {
			Job.getJobs().get(token.nextToken()).abort("User request.");
		} else {
			this.puts("Syntax: ABORTJOB jobid");
		}
	}

	/**
	 * Syntax: STARTJOB jobid
	 * 
	 * @param token
	 */
	private void startjob(final StringTokenizer token) {
		if (token.hasMoreTokens()) {
			Job j = Job.getJobs().get(token.nextToken());
			j.startBlocking();
		} else {
			this.puts("Syntax: STARTJOB jobid");
		}
	}

	/**
	 * Syntax: NEWJOB path_to_formula path_to_outputfile solverid heuristic
	 * cores timeout
	 * 
	 * @param token
	 */
	private void newjob(final StringTokenizer token) {
		try {
			String input_path = token.nextToken();
			String output_path = token.nextToken();
			String solverid = token.nextToken();
			String heuristicId = token.nextToken();
			int maxCores = Integer.parseInt(token.nextToken());
			long timeout = Long.parseLong(token.nextToken());
			Heuristic h = HeuristicFactory.getHeuristic(heuristicId);
			new Job(input_path, output_path, solverid, h, timeout, maxCores);

		} catch (NoSuchElementException e) {
			this.puts("Syntax: NEWJOB path_to_formula path_to_outputfile solverid heuristic max_cores timeout");
		} catch (Exception e) {
			LOGGER.error("Could not instantiate heuristic class", e);
		}
	}

	private void quit() {
		this.run = false;
	}

	private void put(final String s) {
		System.out.print(s);
	}

	private void puts(final String s) {
		System.out.println(s);
	}

	private String read() {
		String line = null;
		try {
			line = this.in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}

	@Override
	public void update(final Observable o, final Object arg) {
		synchronized (this) {
			this.notifyAll();
		}
	}

}
