//package main.java.slave;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//import org.apache.log4j.Logger;
//
//public class FormulaListener implements Runnable{
//
//	static Logger logger = Logger.getLogger(FormulaListener.class);
//	
//	ServerSocket sock;
//	
//	private boolean run = true;
//	
//	public FormulaListener(int port) throws IOException {
//		sock = new ServerSocket(port);
//		logger.info("Slave listening for formulas on port " + port);
//	}
//	
//	@Override
//	public void run() {
//		while(run) {
//			Socket clientSock;
//			try {
//				clientSock = sock.accept();
//				Slave.globalThreadPool.execute(new FormulaReceiver(clientSock, this));
//			} catch (IOException e) {
//				if(run)
//					logger.error("FormulaReceiver not created", e);
//			}
//		}
//	}
//
//	public void stop() {
//		this.run = false;
//		try {
//			sock.close();
//		} catch (IOException e) {
//			logger.error("Cant close socket", e);
//		}
//	}
//	
//}
