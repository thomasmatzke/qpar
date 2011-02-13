package main.java.slave;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.zip.GZIPInputStream;

import main.java.logic.TransmissionQbf;

import org.apache.log4j.Logger;


public class FormulaReceiver implements Runnable {

	static Logger logger = Logger.getLogger(FormulaReceiver.class);
	
	FormulaListener listener;
	private Socket sock;
	private ObjectInputStream ois;
	
	public FormulaReceiver(Socket sock, FormulaListener listener) {
		this.sock = sock;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		try {
			//ois = new ObjectInputStream(sock.getInputStream());
			BufferedInputStream bis = new BufferedInputStream(sock.getInputStream());
			ois = new ObjectInputStream(new GZIPInputStream(bis));
			TransmissionQbf tqbf = (TransmissionQbf) ois.readObject();
			ois.close();
			sock.close();
			logger.info("Received formula " + tqbf.getId());
			ComputationStateMachine m = new ComputationStateMachine(tqbf);
			ComputationStateMachine.computations.put(tqbf.getId(), m);
			m.startComputation();
		} catch (IOException e) {
			logger.error("Couldnt read object", e);
		} catch (ClassNotFoundException e) {
			logger.error("Weird... O_o" + e);
		}
						
	}

}
