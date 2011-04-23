package main.java.common.rmi;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterRemote extends Remote {
	public void registerSlave(SlaveRemote ref) throws RemoteException, UnknownHostException;
	public void ping() throws RemoteException;
	public void displaySlaveMessage(String slave, String message) throws RemoteException;
	public Boolean getCachedResult(byte[] hash) throws RemoteException;
	public void cacheResult(byte[] hash, boolean result) throws RemoteException;
}
