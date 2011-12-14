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
package qpar.slave;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class BeaconListener {

	static Logger logger = Logger.getLogger(BeaconListener.class);
	
	byte[] b = new byte[4];
	DatagramPacket dgram = new DatagramPacket(b, b.length);
	MulticastSocket socket = new MulticastSocket(12345);
	
	InetAddress current;
	
	Slave slave;
	
	public BeaconListener() throws UnknownHostException, IOException {
		socket.joinGroup(InetAddress.getByName("235.1.1.1"));
	}
	
	public String getMasterAddress() throws IOException {
		logger.info("Listening for beacon...");
		socket.receive(dgram);
		return InetAddress.getByAddress(b).getHostAddress();
		
//		InetAddress newAddress;
//		try {
//			
//			newAddress = ;
//			if(current == null) {
//				// Received address. wakeup calling thread
//				current = newAddress;
//				slave.masterIp = newAddress.getHostAddress();
//				synchronized(slave) { slave.notify(); }
//				logger.info("Received server beacon. Beacon contains server address " + newAddress);
//			} else if(current != newAddress) {
//				// The master changed... kill everything and register with new master
//				current = newAddress;
//				logger.info("Received CHANGED server beacon. Beacon contains server address " + newAddress);
//				slave.masterIp = newAddress.getHostAddress();
//				slave.connect();
//			}
//		} catch (IOException e) {
//			logger.error("Problem while listening for beacon", e);
//		}
		
	}

}
