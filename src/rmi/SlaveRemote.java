package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import main.java.messages.FormulaMessage;
import main.java.messages.InformationMessage;

public interface SlaveRemote extends Remote {
	
	public InformationMessage getInformation() throws RemoteException;
	
	public void compute(FormulaMessage m) throws RemoteException;
	
	public void shutdown() throws RemoteException;
	
	public void kill() throws RemoteException;
	
	public void ping() throws RemoteException;
	
}
