//package main.java.slave.solver;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.rmi.RemoteException;
//import java.util.Date;
//
//import main.java.common.rmi.InterpretationData;
//import main.java.common.rmi.TQbfRemote;
//import main.java.slave.tree.QproRepresentation;
//import main.java.slave.tree.ReducedInterpretation;
//
//import org.apache.commons.exec.CommandLine;
//import org.apache.commons.exec.DefaultExecuteResultHandler;
//import org.apache.commons.exec.DefaultExecutor;
//import org.apache.commons.exec.ExecuteException;
//import org.apache.commons.exec.ExecuteWatchdog;
//import org.apache.commons.exec.Executor;
//import org.apache.commons.exec.PumpStreamHandler;
//import org.apache.commons.exec.ShutdownHookProcessDestroyer;
//
//public class CirquitSolver extends Solver {
//
//	private Date overheadStartedAt;
//	private Date overheadStoppedAt;
//	private Date processStartedAt;
//	private Date processStoppedAt;
//	private ExecuteWatchdog watchdog;
//	
//	private File tmpFile;
//
//	public CirquitSolver(TQbfRemote tqbf) {
//		super(tqbf);		 
//	}
//	
//	@Override
//	synchronized public void kill() {
//		if(this.run == false)
//			return;
//		if(watchdog != null)
//			watchdog.destroyProcess();
//		this.killed = true;
//	}
//
//	@Override
//	public void run() {
//		if(killed) {
//			this.error();
//			return;
//		}
//		this.overheadStartedAt = new Date();
//		
//		try {
//			this.tmpFile = File.createTempFile(this.tqbfId, ".qpro");
//		} catch (IOException e1) {
//			logger.error(e1);
//			this.error();
//			return;
//		}
//		
//		InterpretationData id = null;
//		try {
//			id = tqbf.getWorkUnit();
//		} catch (RemoteException e2) {
//			logger.error("", e2);
//		}
//		
//		if(id.getRootNode() == null) {
//			this.error();
//			return;
//		}
//		
//		ReducedInterpretation ri;
//		try {			
//			ri = new ReducedInterpretation(id);
//		} catch (Exception e) {
//			logger.error("Tqbf " + this.tqbfId, e);
//			this.error();
//			return;
//		}
//			
//		this.overheadStoppedAt = new Date();
//		
//		if(ri.isTruthValue()) {
//			logger.info("Formula " + this.tqbfId + " collapsed");
//			this.terminate(ri.getTruthValue(), 0, this.overheadMillis());
//			return;
//		}
//		
//		Executor executor = new DefaultExecutor();
//
//		watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
//		executor.setWatchdog(watchdog);
//
//		ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
//		executor.setProcessDestroyer(processDestroyer);
//
//		CommandLine command = new CommandLine("CirQit2.1");
//		command.addArgument(tmpFile.getAbsolutePath());
//		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
//
//		QproRepresentation qproInput = new QproRepresentation(ri); 
//		FileWriter fw;
//		try {
//			fw = new FileWriter(this.tmpFile);
//			fw.write(qproInput.getQproRepresentation());
//		} catch (IOException e) {
//			logger.error("", e);
//			this.error();
//			return;
//		}	
//		
//		ByteArrayOutputStream output = new ByteArrayOutputStream();
//
//		executor.setStreamHandler(new PumpStreamHandler(output, null, null));
//		
//		try {
//			synchronized(this) {
//				if (killed) {
//					Solver.solvers.remove(this);
//					return;
//				}
//				logger.info("Starting qpro process... (" + tqbfId + ")");
//				this.run = true;
//				executor.execute(command, resultHandler);
//				this.processStartedAt = new Date();
//			}
//		} catch (ExecuteException e) {
//			logger.error("", e);
//			this.error();
//			return;
//		} catch (IOException e) {
//			logger.error("", e);
//			this.error();
//			return;
//		}
//		
////		Slowdown for testing purposes	
////		try {
////			Thread.sleep(5000);
////		} catch (InterruptedException e2) {
////			e2.printStackTrace();
////		}
//		
//		while (!resultHandler.hasResult()) {
//			try {
////				logger.info("waitsfor " + tqbf.getTimeout() * 1000 + " ms");
//				resultHandler.waitFor(this.timeout * 1000);
//				watchdog.destroyProcess();
//				solvers.remove(this.tqbfId);
//			} catch (InterruptedException e1) {
//			}
//			
//		}
//		this.processStoppedAt = new Date();
//		logger.info("qpro process terminated... (" + tqbfId + ")");
//				
////		try {
//////TODO			handleResult(output.toString("ISO-8859-1"));
////		} catch (UnsupportedEncodingException e) {
////			logger.error("", e);
////			this.error();
////			return;
////		}
////		Solver.solvers.remove(this);
//
//	}
//	
//	
//	public long solverMillis() {
//		return this.processStoppedAt.getTime() - this.processStartedAt.getTime();
//	}
//	
//	public long overheadMillis() {
//		return this.overheadStoppedAt.getTime()	- this.overheadStartedAt.getTime();
//	}
//
//}
