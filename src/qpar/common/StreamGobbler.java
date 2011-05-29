package qpar.common;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class StreamGobbler implements Runnable {

	static Logger logger = Logger.getLogger(StreamGobbler.class);
	
	private InputStream is;

	public String result = "";

	public StreamGobbler(InputStream is) {
		this.is = is;
	}

//	@Override
//	public void run() {
//		try {
//			int c = is.read();
//			result += Character.toString((char)c);
//			is.close();
//		} catch (IOException e) {
//			logger.error("IO Error while reading char, or closing stream", e);
//			try { is.close(); } catch (IOException e1) {}
//		}
//	}

	@Override
	public void run() {
		int c;
        try {
			while ((c = is.read()) != -1) {
			    result += Character.toString((char)c);
			}
			logger.info("Reading finished: " + result);
			is.close();
			logger.info("Stream closed");
        } catch (IOException e) {
			logger.error("IO Error while reading char, or closing stream", e);
			try { is.close(); } catch (IOException e1) {}
		}
	}

//	@Override
//	public void run() {
//		try {
//			BufferedReader br = new BufferedReader(new InputStreamReader(is));
//			StringBuilder sb = new StringBuilder();
//			String line = null;
//			while ((line = br.readLine()) != null) {
//				sb.append(line + "\n");
//				System.out.println("OMNOMNOM: " + line);
//			}
//			br.close();
//			result = sb.toString();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}