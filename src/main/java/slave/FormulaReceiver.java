package main.java.slave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import main.java.logic.TransmissionQbf;

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
			ois = new ObjectInputStream(sock.getInputStream());
			TransmissionQbf tqbf = (TransmissionQbf) ois.readObject();
			logger.info("Received formula " + tqbf.getId());
			synchronized(listener.store) {
				listener.store.put(tqbf.getId(), tqbf);
			}
		} catch (IOException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		}
		try {
			ois.close();
			sock.close();
		} catch (IOException e) {
			logger.error(e);
		}		
		
	}

}
