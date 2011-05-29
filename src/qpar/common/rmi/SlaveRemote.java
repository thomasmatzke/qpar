package qpar.common.rmi;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface SlaveRemote extends Remote {
		
//	public void computeFormula(TQbfRemote tqbf) throws RemoteException;
	
	public void shutdown() throws RemoteException;
	
	public void kill(String reason) throws RemoteException;
		
	public ArrayList<String> getSolvers() throws RemoteException;
	
	public int getCores() throws RemoteException;

	public String getHostName() throws RemoteException, UnknownHostException;
	
	public void abortFormula(String tqbfId) throws RemoteException;
	
	public int getRunningComputations()  throws RemoteException;
	
}
