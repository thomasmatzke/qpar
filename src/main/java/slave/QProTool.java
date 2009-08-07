package main.java.slave;

import main.java.logic.Qbf;

public class QProTool implements Tool {

	private Boolean result = null;
	private Process qpro_process;
	
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void kill() {
		// TODO Auto-generated method stub
		// qpro_process.destroy();
		cleanup();
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean result() throws Exception {
		if(this.result == null) {
			throw new Exception("Computation not yet completed");
		}
		return this.result.booleanValue();
	}

	@Override
	public void setQbf(Qbf formula) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		prepare();
		// ProcessBuilder pb = new ProcesBuilder("qpro-etc..");
		// qpro_process = pb.start;
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.result = new Boolean(true);
	}
	
	protected void finalize() throws Throwable {
		cleanup();
	}
}
