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
package qpar.master;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.dom.heuristic.Heuristic;
import qpar.master.Job.State;
import qpar.master.heuristic.HeuristicFactory;

public class Evaluation implements Observer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Evaluation.class);
	/**
	 * [file][cores][heuristic]
	 */
	Job results[][][];

	int coresStart;
	int coresEnd;
	String solver;
	List<String> heuristics;
	long timeout;
	List<File> files = new ArrayList<File>();

	List<Job> jobsTodo = new ArrayList<Job>();
	Date startedAt = null;
	Date stoppedAt = null;
	File dir;

	public Evaluation(final File dir, final int coresStart, final int coresEnd, final String solver, final List<String> heuristics,
			final long timeout) {
		if (!this.isBaseTwo(coresStart) || !this.isBaseTwo(coresEnd)) {
			IllegalArgumentException e = new IllegalArgumentException("Use only powers of 2");
			throw e;
		}
		this.coresStart = coresStart;
		this.coresEnd = coresEnd;
		this.solver = solver;
		this.heuristics = heuristics;
		this.timeout = timeout;
		this.dir = dir;

		for (File f : dir.listFiles()) {
			if (f.getName().equals("evaluation.txt")) {
				continue;
			}
			this.files.add(f);
		}

		this.results = new Job[this.files.size()][this.getNeededRuns(coresStart, coresEnd).size()][heuristics.size()];
	}

	private boolean allJobsTerminated() {
		for (Job j : this.jobsTodo) {
			if (j.getStatus() == Job.State.READY || j.getStatus() == Job.State.RUNNING) {
				return false;
			}
		}
		return true;
	}

	private String detailedFileReport(final int f) {
		StringBuffer sbuf = new StringBuffer();

		for (int h = 0; h < this.heuristics.size(); h++) {
			sbuf.append("Heuristic: " + this.heuristics.get(h) + "\n");
			for (int c = 0; c < this.getNeededRuns(this.coresStart, this.coresEnd).size(); c++) {
				switch (this.results[f][c][h].getStatus()) {
				case TIMEOUT:
					sbuf.append("T");
					break;
				case ERROR:
					sbuf.append("E");
					break;
				case COMPLETE:
					if (this.results[f][c][h].getResult()) {
						sbuf.append("t");
					} else {
						sbuf.append("f");
					}
					break;
				default:
					sbuf.append("!");
					break;
				}
			}
			sbuf.append("\n");
		}

		return sbuf.toString().trim();
	}

	private String detailedReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Detailed Report\n");

		for (int f = 0; f < this.files.size(); f++) {
			sbuf.append("File: " + this.files.get(f) + "\n");
			sbuf.append(this.detailedFileReport(f) + "\n\n");
		}

		return sbuf.toString().trim();
	}

	public void evaluate() {
		this.startedAt = new Date();
		for (int f = 0; f < this.files.size(); f++) {
			for (int c = 0; c < this.getNeededRuns(this.coresStart, this.coresEnd).size(); c++) {
				for (int h = 0; h < this.heuristics.size(); h++) {
					Job j;
					try {
						Heuristic heuristic = HeuristicFactory.getHeuristic(this.heuristics.get(h));
						j = new Job(this.files.get(f).getAbsolutePath(), null, this.solver, heuristic, this.timeout, this.getNeededRuns(
								this.coresStart, this.coresEnd).get(c));
						this.results[f][c][h] = j;
						this.jobsTodo.add(j);
						j.addObserver(this);
						j.start();
					} catch (RemoteException e) {
						LOGGER.error("", e);
					} catch (Exception e) {
						LOGGER.error(String.format("Unable to instantiate Heuristic %s", this.heuristics.get(h)), e);
					}
				}
			}
		}

		synchronized (this) {
			while (!this.allJobsTerminated()) {
				LOGGER.info("Checking termination");
				try {
					this.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		for (Job j : this.jobsTodo) {
			j.deleteObserver(this);
		}

		this.stoppedAt = new Date();
		LOGGER.info(this.getReport());
	}

	private ArrayList<Integer> getNeededRuns(final int start, final int end) {
		int start_ = start, end_ = end;
		ArrayList<Integer> runs = new ArrayList<Integer>();

		while (start_ <= end_) {
			runs.add(start_);
			start_ *= 2;
		}

		return runs;
	}

	public String getReport() {
		String report = "Logarithmic Evaluation Suite Report\n" + "Started: " + this.startedAt + "\n" + "Stopped: " + this.stoppedAt + "\n"
				+ "Solvers: \t" + this.heuristics + "\n" + "Timeout: \t" + this.timeout + "\n" + "Cores Min: \t" + this.coresStart + "\n"
				+ "Cores Max: \t" + this.coresEnd + "\n" + "Directory: \t" + this.dir + "\n";
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			report += "Host: \t" + hostname + "\n\n";
		} catch (UnknownHostException e) {
			report += "Host: \t UNKNOWN \n\n";
		}

		if (!this.isCorrect()) {
			report += "RESULTS INCONSISTENT. SOLVER NOT CORRECT\n\n";
		}

		report += this.runtimesReport() + "\n\n";
		report += this.timeoutErrorsReport() + "\n\n";
		report += this.detailedReport() + "\n\n";
		report += this.meanSolvertimesReport() + "\n\n";
		report += this.maxSolvertimesReport() + "\n\n";
		report += this.meanOverheadReport() + "\n\n";

		return report;
	}

	private boolean isBaseTwo(final int i) {
		double ld = Math.log(i) / Math.log(2);
		if (Math.floor(ld) == ld) {
			return true;
		}
		return false;
	}

	public boolean isCorrect() {
		for (int f = 0; f < this.files.size(); f++) {
			Boolean fileIs = null;
			for (int c = 0; c < this.getNeededRuns(this.coresStart, this.coresEnd).size(); c++) {
				for (int h = 0; h < this.heuristics.size(); h++) {
					if (fileIs == null && !(this.results[f][c][h].isTimeout() || this.results[f][c][h].isError())) {
						fileIs = this.results[f][c][h].getResult();
						continue;
					}
					if (this.results[f][c][h].isTimeout() || this.results[f][c][h].isError()) {
						continue;
					}
					if (!fileIs.equals(this.results[f][c][h].getResult())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private String maxSolvertimeLine(final int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < this.heuristics.size(); h++) {
			double cumulatedTime = 0;

			for (int f = 0; f < this.files.size(); f++) {
				cumulatedTime += this.results[f][i][h].maxSolverTime();
			}
			sbuf.append(String.format("%.2f", cumulatedTime / 1000.00) + "\t");
		}

		String ret = sbuf.toString();
		return ret.trim();
	}

	private String maxSolvertimesReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Added max solvertimes \n");
		String line = "";
		for (String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s\t", h);
		}
		sbuf.append(line + "\n");

		for (int i = 0; i < this.getNeededRuns(this.coresStart, this.coresEnd).size(); i++) {
			line = "";
			String cores = this.getNeededRuns(this.coresStart, this.coresEnd).get(i).toString();
			line += cores + "\t";
			line += this.maxSolvertimeLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String meanOverheadReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Added mean overheadtimes \n");
		String line = "";
		for (String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s\t", h);
		}
		sbuf.append(line + "\n");

		for (int i = 0; i < this.getNeededRuns(this.coresStart, this.coresEnd).size(); i++) {
			line = "";
			String cores = this.getNeededRuns(this.coresStart, this.coresEnd).get(i).toString();
			line += cores + "\t";
			line += this.meanOverheadtimeLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String meanOverheadtimeLine(final int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < this.heuristics.size(); h++) {
			double cumulatedTime = 0;

			for (int f = 0; f < this.files.size(); f++) {
				cumulatedTime += this.results[f][i][h].meanOverheadTime();
			}
			sbuf.append(String.format("%.2f", cumulatedTime / 1000.00) + "\t");
		}

		String ret = sbuf.toString();
		return ret.trim();
	}

	private String meanSolvertimeLine(final int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < this.heuristics.size(); h++) {
			double cumulatedTime = 0;

			for (int f = 0; f < this.files.size(); f++) {
				cumulatedTime += this.results[f][i][h].meanSolverTime();
			}
			sbuf.append(String.format("%.2f", cumulatedTime / 1000.00) + "\t");
		}

		String ret = sbuf.toString();
		return ret.trim();
	}

	private String meanSolvertimesReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Added mean solvertimes \n");
		String line = "";
		for (String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s\t", h);
		}
		sbuf.append(line + "\n");

		for (int i = 0; i < this.getNeededRuns(this.coresStart, this.coresEnd).size(); i++) {
			line = "";
			String cores = this.getNeededRuns(this.coresStart, this.coresEnd).get(i).toString();
			line += cores + "\t";
			line += this.meanSolvertimeLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String runtimeLine(final int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < this.heuristics.size(); h++) {
			long cumulatedTime = 0;

			for (int f = 0; f < this.files.size(); f++) {
				cumulatedTime += this.results[f][i][h].totalMillis();
			}
			sbuf.append(String.format("%.2f", cumulatedTime / 1000.00) + "\t");
		}

		String ret = sbuf.toString();
		return ret.trim();
	}

	private String runtimesReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Run times \n");
		String line = "";
		for (String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s_total\t", h);
		}
		sbuf.append(line + "\n");

		for (int i = 0; i < this.getNeededRuns(this.coresStart, this.coresEnd).size(); i++) {
			line = "";
			String cores = this.getNeededRuns(this.coresStart, this.coresEnd).get(i).toString();
			line += cores + "\t";
			line += this.runtimeLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String timeoutErrorsLine(final int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < this.heuristics.size(); h++) {
			int cumulatedTimeouts = 0;
			int cumulatedErrors = 0;

			for (int f = 0; f < this.files.size(); f++) {
				if (this.results[f][i][h].getStatus() == State.ERROR) {
					cumulatedErrors++;
				} else if (this.results[f][i][h].getStatus() == State.TIMEOUT) {
					cumulatedTimeouts++;
				}

			}
			sbuf.append(String.format("%d\t%d", cumulatedTimeouts, cumulatedErrors) + "\t");
		}

		String ret = sbuf.toString();
		return ret.trim();
	}

	private String timeoutErrorsReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Timeouts and Errors \n");
		String line = "";
		for (String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s_timeouts\t%s_errors\t", h, h);
		}
		sbuf.append(line + "\n");

		for (int i = 0; i < this.getNeededRuns(this.coresStart, this.coresEnd).size(); i++) {
			line = "";
			String cores = this.getNeededRuns(this.coresStart, this.coresEnd).get(i).toString();
			line += cores + "\t";
			line += this.timeoutErrorsLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	/*
	 * Gets notified about job status changes
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(final Observable arg0, final Object arg1) {
		this.notifyAll();
		int running = 0;
		int error = 0;
		int complete = 0;
		int timeout = 0;
		for (Job jo : this.jobsTodo) {
			switch (jo.getStatus()) {
			case RUNNING:
				running++;
				break;
			case ERROR:
				error++;
				break;
			case COMPLETE:
				complete++;
				break;
			case TIMEOUT:
				timeout++;
				break;
			default:
				break;
			}
		}
		LOGGER.info("Jobs RUNNING: " + running + ", ERROR: " + error + ", COMPLETE: " + complete + ", TIMEOUT: " + timeout);

	}

}
