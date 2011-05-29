package qpar.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SolverRemote extends Remote {

	public void kill() throws RemoteException;
	
}
