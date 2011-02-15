package main.java.master;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.zip.GZIPOutputStream;

import main.java.logic.TransmissionQbf;
import main.java.rmi.SlaveRemote;

import org.apache.commons.io.output.CountingOutputStream;

class TransportThread implements Runnable {
	TransmissionQbf sub = null;
	String solver = null;
	SlaveRemote s = null;
	Socket senderSocket;
	private ObjectOutputStream oos;

	public TransportThread(SlaveRemote s, TransmissionQbf sub, String solver)
			throws UnknownHostException, RemoteException, IOException {
		this.sub = sub;
		this.solver = solver;
		this.s = s;
		// logger.info("hostname: " + s.getHostName());
		senderSocket = new Socket(s.getHostName(), 11111);
	}

	@Override
	public void run() {
		try {
			Job.logger.info("Sending formula " + sub.getId() + " ...");
			// oos = new ObjectOutputStream(senderSocket.getOutputStream());

			BufferedOutputStream bos = new BufferedOutputStream(
					senderSocket.getOutputStream());
			CountingOutputStream cos = new CountingOutputStream(bos);
			oos = new ObjectOutputStream(new GZIPOutputStream(cos));
			long start = System.currentTimeMillis();
			oos.writeObject(sub);
			long stop = System.currentTimeMillis();
			oos.flush();
			oos.close();
			senderSocket.close();
			double time = (stop - start) / 1000.00;
			long kiB = cos.getByteCount() / 1024;
			Job.logger.info("Formula " + sub.getId() + " sent ... (" + kiB
					+ "kiB, " + time + " seconds, " + kiB / time + "kiB/s)");
		} catch (IOException e) {
			Job.logger.error("While sending formula " + sub.getId(), e);
		}
	}
}