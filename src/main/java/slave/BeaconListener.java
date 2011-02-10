package main.java.slave;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class BeaconListener implements Runnable {

	static Logger logger = Logger.getLogger(BeaconListener.class);
	
	byte[] b = new byte[4];
	DatagramPacket dgram = new DatagramPacket(b, b.length);
	MulticastSocket socket = new MulticastSocket(12345);
	
	InetAddress current;
	
	Slave slave;
	
	public BeaconListener(Slave slave) throws UnknownHostException, IOException {
		this.slave = slave;
		socket.joinGroup(InetAddress.getByName("235.1.1.1"));
	}
	
	@Override
	public void run() {
		InetAddress newAddress;
		try {
			socket.receive(dgram);
			newAddress = InetAddress.getByAddress(b);
			if(current == null) {
				// Received address. wakeup calling thread
				current = newAddress;
				slave.masterIp = newAddress.getHostAddress();
				synchronized(slave) { slave.notify(); }
				logger.info("Received server beacon. Beacon contains server address " + newAddress);
			} else if(current != newAddress) {
				// The master changed... kill everything and register with new master
				current = newAddress;
				logger.info("Received CHANGED server beacon. Beacon contains server address " + newAddress);
				slave.masterIp = newAddress.getHostAddress();
				slave.reconnect();
			}
		} catch (IOException e) {
			logger.error("Problem while listening for beacon", e);
		}
		
	}

}
