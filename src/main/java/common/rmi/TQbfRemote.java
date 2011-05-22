package main.java.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import main.java.master.TQbf.State;

public interface TQbfRemote extends Remote {
	public long getTimeout() throws RemoteException;
	public String getJobId() throws RemoteException;
	public String getId() throws RemoteException;
	public InterpretationData getWorkUnit() throws RemoteException;
	public String getSolverId() throws RemoteException;
//	public void compute(SlaveRemote slave) throws RemoteException;
	public void timeout() throws RemoteException;
	public void terminate(boolean isSolvable) throws RemoteException;
	public void error() throws RemoteException;
	public void setOverheadMillis(long millis) throws RemoteException;
	public void setSolverMillis(long millis) throws RemoteException;
	public void setSlave(SlaveRemote slave) throws RemoteException;
	public void setState(State state) throws RemoteException;
	public State getState() throws RemoteException;
	
	public boolean isAborted() throws RemoteException;
	public boolean isComputing() throws RemoteException;
	public boolean isDontstart() throws RemoteException;
	public boolean isError() throws RemoteException;
	public boolean isMerged() throws RemoteException;
	public boolean isNew() throws RemoteException;
	public boolean isTerminated() throws RemoteException;
	public boolean isTimeout() throws RemoteException;
}
