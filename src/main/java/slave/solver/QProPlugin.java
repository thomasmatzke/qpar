package main.java.slave.solver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.java.slave.tree.QproRepresentation;
import main.java.slave.tree.ReducedInterpretation;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;

public class QProPlugin implements SolverPlugin {
	static Logger logger = Logger.getLogger(QProPlugin.class);
	
	
	ReducedInterpretation ri;
	Lock killLock = new ReentrantLock();
	ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
	boolean killed = false, started = false, terminated = false;
	Executor executor = new DefaultExecutor();
	
	Boolean returnValue = null;
	Exception errorValue = null; 
	
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
			QproRepresentation qproInput = new QproRepresentation(ri);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(qproInput.getQproRepresentation().getBytes("ISO-8859-1"));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			executor.setStreamHandler(new PumpStreamHandler(output, null, inputStream));
//			logger.info("QPROINPUT: " + qproInput.getQproRepresentation());
			
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
				logger.error(output);
				throw new Exception(output);
			}
		} finally {
			killLock.unlock();
		}
	}
	
}
