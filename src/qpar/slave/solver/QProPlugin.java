package qpar.slave.solver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;

import qpar.slave.tree.QproRepresentation;
import qpar.slave.tree.ReducedInterpretation;
import qpar.slave.tree.SimpleQProRepresentation;

public class QProPlugin implements SolverPlugin {
	static Logger logger = Logger.getLogger(QProPlugin.class);
	
	
	ReducedInterpretation ri;
	Lock killLock = new ReentrantLock();
	ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
	boolean killed = false, started = false, terminated = false;
	Executor executor = new DefaultExecutor();
	
	Boolean returnValue = null;
	Exception errorValue = null;


	private QproRepresentation qproRepresentation;


	private String qproInputString; 
	
	@Override
	public void initialize(ReducedInterpretation ri) throws Exception {
		this.ri = ri;
	}

	@Override
	public void kill() {
		killLock.lock();
		try {
			
			if(started)
				watchdog.destroyProcess();
			killed = true;
			
		} finally {
			killLock.unlock();
		}
	}

	@Override
	public void run() {
		try {
			executor.setWatchdog(watchdog);
			CommandLine command = new CommandLine("qpro");
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			
			qproRepresentation = new QproRepresentation(ri);
			qproInputString = qproRepresentation.getQproRepresentation();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(qproInputString.getBytes("ISO-8859-1"));
			
			//////
//			SimpleQProRepresentation sqr = new SimpleQProRepresentation(ri);
//			qproInputString = sqr.getQproRepresentation();
//			ByteArrayInputStream inputStream = new ByteArrayInputStream(qproInputString.getBytes("ISO-8859-1"));
			//////
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			executor.setStreamHandler(new PumpStreamHandler(output, null, inputStream));
			//logger.debug("QPROINPUT: \n" + qproRepresentation.getQproRepresentation());
			
			killLock.lock();
			if(killed) {
				killLock.unlock();
				return;
			} else {
				executor.execute(command, resultHandler);
				started = true;
			}
			killLock.unlock();
			
			while (!resultHandler.hasResult()) {
				try {
					resultHandler.waitFor();
					watchdog.destroyProcess();
				} catch (InterruptedException e1) { logger.error("", e1);}
				
			}
			
			returnValue = interpretQproOutput(output.toString("ISO-8859-1"));
			
		} catch (Exception e) {
			ri.getInterpretation().dump("++");
			errorValue = e;
		} finally {
			synchronized(this) {
				terminated = true;
				notifyAll();
			}
		}

	}

	@Override
	synchronized public Boolean waitForResult() throws Exception {
		while(!terminated) {
			wait();
		}
		if(errorValue != null)
			throw errorValue;
		
		return returnValue;
	}

	private Boolean interpretQproOutput(String output) throws Exception {
		killLock.lock();
		try {
			if(this.killed)
				return null;	
		
			if (output.startsWith("1")) {
				return true;
			} else if (output.startsWith("0")) {
				return false;
			} else {
				logger.error(String.format("Qpro returned no result. Treating as error. \nString returned: %s \nQPro input: %s", output, qproInputString));
				throw new Exception(output);
			}
		} finally {
			killLock.unlock();
		}
	}
	
}
