package main.java.rmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.net.Socket;

public class ZipSocket extends Socket {
	private InputStream in;
	private OutputStream out;

	public ZipSocket() { super(); }
	public ZipSocket(String host, int port) 
	throws IOException {
		super(host, port);
	}

	public InputStream getInputStream() 
	throws IOException {
		if (in == null) {
			//in = new DeflaterInputStream(super.getInputStream());
			//in = new ZipInputStream(super.getInputStream());
			//in = new GZIPInputStream(super.getInputStream());
			//in = new CompressionInputStream(super.getInputStream());
		}
		return in;
	}

	public OutputStream getOutputStream() 
	throws IOException {
		if (out == null) {
			//out = new DeflaterOutputStream(super.getOutputStream());
			//out = new ZipOutputStream(super.getOutputStream());
			//out = new GZIPOutputStream(super.getOutputStream());
			//out = new CompressionOutputStream(super.getOutputStream());
		}
		return out;
	}

	public synchronized void close() throws IOException {
		OutputStream o = getOutputStream();
		o.flush();
		super.close();
	}
}
