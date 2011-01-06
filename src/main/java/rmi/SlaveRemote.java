package main.java.rmi;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

import main.java.logic.TransmissionQbf;
import main.java.slave.solver.Solver;

public interface SlaveRemote extends Remote {
		
	public void computeFormula(TransmissionQbf formula, String solverId) throws RemoteException;
	
	public void shutdown() throws RemoteException;
	
	public void kill(String reason) throws RemoteException;
		
	public ArrayList<String> getSolvers() throws RemoteException;
	
	public int getCores() throws RemoteException;

	public String getHostName() throws RemoteException, UnknownHostException;
	
	public void abortFormula(String tqbfId) throws RemoteException;
	
	public Hashtable<String, Solver> getThreads() throws RemoteException;
	
	public String[] getCurrentJobs() throws RemoteException;
	
}
