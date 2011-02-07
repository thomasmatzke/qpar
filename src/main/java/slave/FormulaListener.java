package main.java.slave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import main.java.logic.TransmissionQbf;

import org.apache.log4j.Logger;

public class FormulaListener implements Runnable{

	static Logger logger = Logger.getLogger(FormulaListener.class);
	
	ServerSocket sock;

	public HashMap<String, TransmissionQbf> store = new HashMap<String, TransmissionQbf>();
	
	public FormulaListener(int port) throws IOException {
		sock = new ServerSocket(port);
		logger.info("Slave listening for formulas on port " + port);
	}
	
	@Override
	public void run() {
		while(true) {
			Socket clientSock;
			try {
				clientSock = sock.accept();
				new Thread(new FormulaReceiver(clientSock, this)).start();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	public TransmissionQbf getTqbf(String id) {
		synchronized(store) {
			return store.get(id);
		}
	}
	
	public void deleteFormula(String id) {
		synchronized(store) {
			store.remove(id);
		}
	}
	
}
