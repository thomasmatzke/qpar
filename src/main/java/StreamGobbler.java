package main.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class StreamGobbler extends Thread {
	InputStream is;
	public String readString;

	public StreamGobbler(InputStream is) {
		this.is = is;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			StringWriter writer = new StringWriter();
			IOUtils.copy(isr, writer);
			readString = writer.toString();
		} catch (IOException ioe) {
			readString = "";
			ioe.printStackTrace();
		}
	}
}
