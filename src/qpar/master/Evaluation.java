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

import org.apache.log4j.Logger;

import qpar.master.Job.State;
import qpar.master.heuristic.Heuristic;
import qpar.master.heuristic.HeuristicFactory;

public class Evaluation implements Observer {

	static Logger logger = Logger.getLogger(Evaluation.class);
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

	public Evaluation(File dir, int coresStart, int coresEnd,
			String solver, List<String> heuristics, long timeout) {
		if (!isBaseTwo(coresStart) || !isBaseTwo(coresEnd)) {
			IllegalArgumentException e = new IllegalArgumentException(
					"Use only powers of 2");
			throw e;
		}
		this.coresStart = coresStart;
		this.coresEnd = coresEnd;
		this.solver = solver;
		this.heuristics = heuristics;
		this.timeout = timeout;
		this.dir = dir;

		for (File f : dir.listFiles()) {
			if (f.getName().equals("evaluation.txt"))
				continue;
			files.add(f);
		}

		results = new Job[files.size()][this
				.getNeededRuns(coresStart, coresEnd).size()][heuristics.size()];
	}

	public void evaluate() {
		this.startedAt = new Date();
		for (int f = 0; f < files.size(); f++) {
			for (int c = 0; c < getNeededRuns(coresStart, coresEnd).size(); c++) {
				for (int h = 0; h < heuristics.size(); h++) {
					Job j;
					try {
						Heuristic heuristic = HeuristicFactory.getHeuristic(heuristics.get(h));
						j = new Job(files.get(f).getAbsolutePath(), null,
								solver, heuristic, timeout,
								getNeededRuns(coresStart, coresEnd).get(c));
						results[f][c][h] = j;
						jobsTodo.add(j);
						j.addObserver(this);
						j.start();
					} catch (RemoteException e) {
						logger.error("", e);
					}
				}
			}
		}
		
		synchronized (this) {
			while (!allJobsTerminated()) {
				logger.info("Checking termination");
				try { wait(); } catch (InterruptedException e) {}
			}
		}

		for(Job j : jobsTodo) {
			j.deleteObserver(this);
		}
		
		this.stoppedAt = new Date();
		logger.info(this.getReport());
	}

	public boolean isCorrect() {
		for (int f = 0; f < files.size(); f++) {
			Boolean fileIs = null;
			for (int c = 0; c < getNeededRuns(coresStart, coresEnd).size(); c++) {
				for (int h = 0; h < heuristics.size(); h++) {
					if (fileIs == null && !(results[f][c][h].isTimeout() || results[f][c][h].isError())) {
						fileIs = results[f][c][h].getResult();
						continue;
					}
					if(results[f][c][h].isTimeout() || results[f][c][h].isError())
						continue;
					if (!fileIs.equals(results[f][c][h].getResult()))
						return false;
				}
			}
		}
		return true;
	}

	public String getReport() {
		String report = "Logarithmic Evaluation Suite Report\n" + "Started: "
				+ startedAt + "\n" + "Stopped: " + stoppedAt + "\n"
				+ "Solvers: \t" + heuristics + "\n" + "Timeout: \t" + timeout
				+ "\n" + "Cores Min: \t" + coresStart + "\n" + "Cores Max: \t"
				+ coresEnd + "\n" + "Directory: \t" + dir + "\n";
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			report += "Host: \t" + hostname + "\n\n";
		} catch (UnknownHostException e) {
			report += "Host: \t UNKNOWN \n\n";
		}

		if (!this.isCorrect()) {
			report += "RESULTS INCONSISTENT. SOLVER NOT CORRECT\n\n";
		}

		report += runtimesReport() + "\n\n";
		report += timeoutErrorsReport() + "\n\n";
		report += detailedReport() + "\n\n";
		report += meanSolvertimesReport() + "\n\n";
		report += maxSolvertimesReport() + "\n\n";
		report += meanOverheadReport() + "\n\n";

		return report;
	}

	private String timeoutErrorsReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Timeouts and Errors \n");
		String line = "";
		for (String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s_timeouts\t%s_errors\t", h, h);
		}
		sbuf.append(line + "\n");

		for (int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i)
					.toString();
			line += cores + "\t";
			line += timeoutErrorsLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String timeoutErrorsLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < heuristics.size(); h++) {
			int cumulatedTimeouts = 0;
			int cumulatedErrors = 0;

			for (int f = 0; f < files.size(); f++) {
				if (results[f][i][h].getStatus() == State.ERROR) {
					cumulatedErrors++;
				} else if (results[f][i][h].getStatus() == State.TIMEOUT) {
					cumulatedTimeouts++;
				}

			}
			sbuf.append(String.format("%d\t%d", cumulatedTimeouts,
					cumulatedErrors) + "\t");
		}

		String ret = sbuf.toString();
		return ret.trim();
	}

	private String meanOverheadReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Added mean overheadtimes \n");
		String line = "";
		for (String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s\t", h);
		}
		sbuf.append(line + "\n");

		for (int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i)
					.toString();
			line += cores + "\t";
			line += meanOverheadtimeLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String meanOverheadtimeLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < heuristics.size(); h++) {
			double cumulatedTime = 0;

			for (int f = 0; f < files.size(); f++) {
				cumulatedTime += results[f][i][h].meanOverheadTime();
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

		for (int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i)
					.toString();
			line += cores + "\t";
			line += maxSolvertimeLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String maxSolvertimeLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < heuristics.size(); h++) {
			double cumulatedTime = 0;

			for (int f = 0; f < files.size(); f++) {
				cumulatedTime += results[f][i][h].maxSolverTime();
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

		for (int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i)
					.toString();
			line += cores + "\t";
			line += meanSolvertimeLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String meanSolvertimeLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < heuristics.size(); h++) {
			double cumulatedTime = 0;

			for (int f = 0; f < files.size(); f++) {
				cumulatedTime += results[f][i][h].meanSolverTime();
			}
			sbuf.append(String.format("%.2f", cumulatedTime / 1000.00) + "\t");
		}

		String ret = sbuf.toString();
		return ret.trim();
	}

	private String detailedReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Detailed Report\n");

		for (int f = 0; f < files.size(); f++) {
			sbuf.append("File: " + files.get(f) + "\n");
			sbuf.append(detailedFileReport(f) + "\n\n");
		}

		return sbuf.toString().trim();
	}

	private String detailedFileReport(int f) {
		StringBuffer sbuf = new StringBuffer();

		for (int h = 0; h < heuristics.size(); h++) {
			sbuf.append("Heuristic: " + heuristics.get(h) + "\n");
			for (int c = 0; c < getNeededRuns(coresStart, coresEnd).size(); c++) {
				switch (results[f][c][h].getStatus()) {
				case TIMEOUT:
					sbuf.append("T");
					break;
				case ERROR:
					sbuf.append("E");
					break;
				case COMPLETE:
					if (results[f][c][h].getResult())
						sbuf.append("t");
					else
						sbuf.append("f");
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

	private String runtimesReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Run times \n");
		String line = "";
		for (String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s_total\t", h);
		}
		sbuf.append(line + "\n");

		for (int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i)
					.toString();
			line += cores + "\t";
			line += runtimeLine(i) + "\n";
			sbuf.append(line);
		}

		return sbuf.toString();
	}

	private String runtimeLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for (int h = 0; h < heuristics.size(); h++) {
			long cumulatedTime = 0;

			for (int f = 0; f < files.size(); f++) {
				cumulatedTime += results[f][i][h].totalMillis();
			}
			sbuf.append(String.format("%.2f", cumulatedTime / 1000.00) + "\t");
		}

		String ret = sbuf.toString();
		return ret.trim();
	}

	private boolean isBaseTwo(int i) {
		double ld = Math.log(i) / Math.log(2);
		if (Math.floor(ld) == ld)
			return true;
		return false;
	}

	private ArrayList<Integer> getNeededRuns(int start, int end) {
		ArrayList<Integer> runs = new ArrayList<Integer>();

		while (start <= end) {
			runs.add(start);
			start *= 2;
		}

		return runs;
	}

	/**
	 * Gets notified about job status changes
	 */
	@Override
	synchronized public void update(Observable arg0, Object arg1) {
		notifyAll();
		int running = 0;
		int error = 0;
		int complete = 0;
		int timeout = 0;
		for (Job jo : this.jobsTodo) {
			switch(jo.getStatus()) {
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
		logger.info("Jobs RUNNING: " + running + ", ERROR: " + error + ", COMPLETE: " + complete + ", TIMEOUT: " + timeout);
	}

	private boolean allJobsTerminated() {
		for (Job j : this.jobsTodo) {
			if (j.getStatus() == Job.State.READY
					|| j.getStatus() == Job.State.RUNNING)
				return false;
		}
		return true;
	}

}
