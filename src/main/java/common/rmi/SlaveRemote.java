package main.java.common.rmi;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface SlaveRemote extends Remote {
		
	public void computeFormula(TQbfRemote tqbf) throws RemoteException;
	
	public void shutdown() throws RemoteException;
	
	public void kill(String reason) throws RemoteException;
		
	public ArrayList<String> getSolvers() throws RemoteException;
	
	public int getCores() throws RemoteException;

	public String getHostName() throws RemoteException, UnknownHostException;
	
	public void abortFormula(String tqbfId) throws RemoteException;
	
//	public Hashtable<String, Solver> getThreads() throws RemoteException;
	
//	public void computeTqbf(TQbfRemote tqbf) throws RemoteException;
	
//	public ArrayList<String> getCurrentJobs() throws RemoteException;
	
	public void setMailInfo(String mailServer, String mailUser, String mailPass) throws RemoteException;
	
	public void setExceptionNotifierAddress(String address) throws RemoteException;
	
//	public int freeCores() throws RemoteException;
	
}
