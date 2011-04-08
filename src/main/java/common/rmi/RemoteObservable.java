package main.java.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObservable extends Remote {
    
    void addObserver(RemoteObserver o) throws RemoteException;
    int countObservers() throws RemoteException;
}
