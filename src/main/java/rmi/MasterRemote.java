package main.java.rmi;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterRemote extends Remote {
	
	public void unregisterSlave(SlaveRemote ref) throws RemoteException, UnknownHostException;

	public void registerSlave(SlaveRemote ref) throws RemoteException, UnknownHostException;

	public void returnResult(Result r) throws RemoteException;
	
	public void ping() throws RemoteException;
	
}
