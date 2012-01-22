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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeaconListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeaconListener.class);

	byte[] b = new byte[4];
	DatagramPacket dgram = new DatagramPacket(this.b, this.b.length);
	MulticastSocket socket = new MulticastSocket(12345);

	InetAddress current;

	Slave slave;

	public BeaconListener() throws UnknownHostException, IOException {
		this.socket.joinGroup(InetAddress.getByName("235.1.1.1"));
	}

	public String getMasterAddress() throws IOException {
		LOGGER.info("Listening for beacon...");
		this.socket.receive(this.dgram);
		return InetAddress.getByAddress(this.b).getHostAddress();
	}

}
