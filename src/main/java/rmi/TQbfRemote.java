package main.java.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import main.java.master.TQbf.State;
import main.java.slave.solver.Solver;

public interface TQbfRemote extends Remote, RemoteObservable {
	public void compute(SlaveRemote slave) throws RemoteException;
	public void abort() throws RemoteException;
	public void waitFor() throws RemoteException; 
	public void setResult(Result r) throws RemoteException;
	public Result getResult() throws RemoteException;
	public State getState() throws RemoteException;
	public boolean isComputing() throws RemoteException;
	public long getTimeout() throws RemoteException;
	public String getJobId() throws RemoteException;
	public String getId() throws RemoteException;
	public void timeout() throws RemoteException;
	public InterpretationData getWorkUnit() throws RemoteException;
	public String getSolverId() throws RemoteException;
	public void handleResult(Result r) throws RemoteException;
}
