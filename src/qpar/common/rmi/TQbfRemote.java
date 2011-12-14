/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package qpar.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import qpar.master.TQbf.State;


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
