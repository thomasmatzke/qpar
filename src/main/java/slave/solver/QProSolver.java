package main.java.slave.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import main.java.logic.TransmissionQbf;
import main.java.slave.Master;
import main.java.slave.SlaveDaemon;

public class QProSolver implements Solver {

	static Logger logger = Logger.getLogger(SlaveDaemon.class);
	public static final String toolId = "qpro";
	private Boolean result = null;
	private Process qpro_process;
	private TransmissionQbf formula;
	private Master master;
	
	public Master getMaster() {
		return master;
	}

	public void setMaster(Master master) {
		this.master = master;
	}

	public void cleanup() {}
	
	public void kill() {
		qpro_process.destroy();
		cleanup();
	}

	public void prepare() {}
	
	public boolean result() throws Exception {
		if(this.result == null) {
			throw new Exception("Computation not yet completed");
		}
		return this.result.booleanValue();
	}

	
	public void setTransmissionQbf(TransmissionQbf formula) {
		this.formula = formula;
	}

	public void run() {
		prepare();
		ProcessBuilder pb = new ProcessBuilder("qpro");
		try {
			qpro_process = pb.start();
			PrintWriter stdin = new PrintWriter(qpro_process.getOutputStream());
			BufferedReader stdout = new BufferedReader(new InputStreamReader(qpro_process.getInputStream()));
			stdin.print(toInputString(this.formula));
			
			String firstLine = stdout.readLine();
			if(firstLine == "1") {
				this.result = new Boolean(true);
			} else if (firstLine == "0") {
				this.result = new Boolean(false);
			} else {
				logger.error("Got non-expected result from solver(" + firstLine + "). Aborting Formula.");
				master.sendFormulaAbortedMessage(formula.getId());
			}
		} catch (IOException e) {
			logger.error("IO Error while getting result from solver: " + e.getCause());
			master.sendFormulaAbortedMessage(formula.getId());
		}
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		cleanup();
	}
	
	public static String toInputString(TransmissionQbf t) {
		// TODO
		return "QBF\n4\nq\ne 2\na 3 4\nd\n 2 3 4\n\n/d\n/q\nQBF\n";
	}
	
}
