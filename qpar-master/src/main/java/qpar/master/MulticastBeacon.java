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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MulticastBeacon implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MulticastBeacon.class);

	boolean run = true;

	DatagramSocket socket = null;

	byte[] b; // 500 byte are safe
	DatagramPacket dgram = null;
	InetAddress localIp = null;

	public MulticastBeacon() throws UnknownHostException, SocketException {
		this.socket = new DatagramSocket();

		// Find my real ip (not localhost)
		this.localIp = this.findLocalIp();

		this.b = this.localIp.getAddress();

		this.dgram = new DatagramPacket(this.b, this.b.length, InetAddress.getByName("235.1.1.1"), 12345);
	}

	public void stop() {
		this.run = false;
	}

	public void run() {

		while (this.run) {

			try {
				// logger.info("Beacon Ping...");
				this.socket.send(this.dgram);
			} catch (IOException e) {
				LOGGER.error("Cant send beacon", e);
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
	}

	private InetAddress findLocalIp() throws SocketException {
		for (NetworkInterface ifc : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			if (ifc.isUp()) {
				for (InetAddress addr : Collections.list(ifc.getInetAddresses())) {
					if (!addr.isLoopbackAddress() && (addr.getAddress().length <= 4)) {
						return addr;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void finalize() {
		this.socket.close();
	}

}
