package main.java.slave.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import main.java.logic.TransmissionQbf;

public class QProSolver implements Solver {

	public static final String toolId = "qpro";
	private Boolean result = null;
	private Process qpro_process;
	private TransmissionQbf formula;
	
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
			PrintWriter stdin = new PrintWriter(qpro_process.getOutputStream());
			BufferedReader stdout = new BufferedReader(new InputStreamReader(qpro_process.getInputStream()));
			stdin.print(toInputString(this.formula));
			qpro_process = pb.start();
			String firstLine = stdout.readLine();
			if(firstLine == "1") {
				this.result = new Boolean(true);
			} else if (firstLine == "0") {
				this.result = new Boolean(false);
			} else {
				// TODO: Error. send message to master
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
	
	protected void finalize() throws Throwable {
		cleanup();
	}
	
	public static String toInputString(TransmissionQbf t) {
		// TODO
		return null;
	}
	
}
