package main.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import main.java.master.Mailer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class QPar {
	static Logger logger = Logger.getLogger(QPar.class);
	
	public static Level logLevel = Level.INFO;
	public static String exceptionNotifierAddress = null;
	public static String mailServer = null;
	public static String mailUser = null;
	public static String mailPass = null;
	
	
	public static boolean isMailInfoComplete() {
		return mailServer != null && mailUser != null && mailPass != null;
	}
	
	public static void loadConfig() {
		Properties properties = new Properties();
		try {
		    properties.load(new FileInputStream("qpar.conf"));
		} catch (IOException e) {
			logger.warn("Config file not found.");
		}
		QPar.mailPass 	= properties.getProperty("mailPass");
		QPar.mailUser 	= properties.getProperty("mailUser");
		QPar.mailServer = properties.getProperty("mailServer");
		QPar.exceptionNotifierAddress = properties.getProperty("exceptionNotifierAddress");
	}
	
	public static void sendExceptionMail(Throwable t) {
		String body = "";
		try {
			body += "Host: " + InetAddress.getLocalHost().getHostName() + "\n";
		} catch (UnknownHostException e) {
			body += "Host: UNKNOWN (Exception ocurred)\n";
		}
		body += StackTraceUtil.getStackTrace(t);
		if(QPar.isMailInfoComplete() && QPar.exceptionNotifierAddress != null)
			Mailer.send_mail(QPar.exceptionNotifierAddress, QPar.mailServer, QPar.mailUser, QPar.mailPass, "Exception Notification", body);
	}
	
}
