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
package qpar.master;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;

import org.apache.log4j.Logger;

public class MulticastBeacon implements Runnable {

	static Logger logger = Logger.getLogger(MulticastBeacon.class);
	
	boolean run = true;
	
	DatagramSocket socket = null;

	byte[] b; // 500 byte are safe
	DatagramPacket dgram = null;
	InetAddress localIp = null;
	
	
	public MulticastBeacon() throws UnknownHostException, SocketException {
		socket = new DatagramSocket();

		// Find my real ip (not localhost)
		localIp = findLocalIp();
		
		b = localIp.getAddress();
		
		dgram = new DatagramPacket(b, b.length, InetAddress.getByName("235.1.1.1"), 12345);
	}
	
	public void stop() {
		run = false;
	}
		
	@Override
	public void run() {
		
		while(run) {
			
			try {
//				logger.info("Beacon Ping...");
				socket.send(dgram);
			} catch (IOException e) {
				logger.error("Cant send beacon", e);
			}			
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}
	}

	private InetAddress findLocalIp() throws SocketException {
		for(NetworkInterface ifc : Collections.list(NetworkInterface.getNetworkInterfaces())) {
		   if(ifc.isUp()) {
		      for(InetAddress addr : Collections.list(ifc.getInetAddresses())) {
		    	  if (!addr.isLoopbackAddress() && (addr.getAddress().length <= 4)) {
		    		  return addr;
		    	  }
		      }
		   }
		}
		return null;
	}
	
	public void finalize() {
		socket.close();
	}
	
}
