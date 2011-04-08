package main.java.common.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObserver extends Remote, Serializable {
	void update(Object o, Object arg) throws RemoteException;
}
