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
	
	public static boolean enableExceptionNotifications = false;
	private static String exceptionNotifierAddress = null;
	public static String mailServer = null;
	public static String mailUser = null;
	public static String mailPass = null;
	private static boolean benchmarkMode = false;
	private static boolean resultCaching = false;
	
	public static boolean isResultCaching() {
		return resultCaching;
	}

	public static void setResultCaching(boolean resultCaching) {
		QPar.resultCaching = resultCaching;
	}

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
		QPar.mailPass 						= properties.getProperty("mailPass");
		QPar.mailUser 						= properties.getProperty("mailUser");
		QPar.mailServer 					= properties.getProperty("mailServer");
		QPar.setExceptionNotifierAddress(properties.getProperty("exceptionNotifierAddress"));
		QPar.enableExceptionNotifications 	= Boolean.parseBoolean(properties.getProperty("enableExceptionNotifications"));
		QPar.setBenchmarkMode(Boolean.parseBoolean(properties.getProperty("benchmarkMode")));
		QPar.setResultCaching(Boolean.parseBoolean(properties.getProperty("resultCaching")));
	}
	
	public static void sendExceptionMail(Throwable t) {
		if(!QPar.enableExceptionNotifications)
			return;
		String body = "";
		try {
			body += "Host: " + InetAddress.getLocalHost().getHostName() + "\n";
		} catch (UnknownHostException e) {
			body += "Host: UNKNOWN (Exception ocurred)\n";
		}
		body += StackTraceUtil.getStackTrace(t);
		if(QPar.isMailInfoComplete() && QPar.getExceptionNotifierAddress() != null)
			Mailer.send_mail(QPar.getExceptionNotifierAddress(), QPar.mailServer, QPar.mailUser, QPar.mailPass, "Exception Notification", body);
	}

	public static String getMailServer() {
		return mailServer;
	}

	public static void setMailServer(String mailServer) {
		QPar.mailServer = mailServer;
	}

	public static String getMailUser() {
		return mailUser;
	}

	public static void setMailUser(String mailUser) {
		QPar.mailUser = mailUser;
	}

	public static String getMailPass() {
		return mailPass;
	}

	public static void setMailPass(String mailPass) {
		QPar.mailPass = mailPass;
	}

	public static void setExceptionNotifierAddress(String exceptionNotifierAddress) {
		QPar.exceptionNotifierAddress = exceptionNotifierAddress;
	}

	public static String getExceptionNotifierAddress() {
		return exceptionNotifierAddress;
	}

	public static void setBenchmarkMode(boolean benchmarkMode) {
		QPar.benchmarkMode = benchmarkMode;
	}

	public static boolean isBenchmarkMode() {
		return benchmarkMode;
	}
	
}
