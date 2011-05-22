package main.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import main.java.master.Mailer;

import org.apache.log4j.Logger;

public class QPar {
	static Logger logger = Logger.getLogger(QPar.class);
	
	private static boolean enableExceptionNotifications = false;
	private static String exceptionNotifierAddress = null;
	private static String mailServer = null;
	private static String mailUser = null;
	private static String mailPass = null;
	private static boolean benchmarkMode = false;
	private static boolean resultCaching = false;
	private static boolean mailEvaluationResults = false;
	private static HashMap<String, String> plugins = new HashMap<String, String>();
	
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
		QPar.setMailEvaluationResults(Boolean.parseBoolean(properties.getProperty("mailEvaluationResults")));
		QPar.setResultCaching(Boolean.parseBoolean(properties.getProperty("resultCaching")));
		for(Entry<Object, Object> e : properties.entrySet()) {
			String key = (String)e.getKey();
			String value = (String)e.getValue();
			if(key.startsWith("plugin.")) {
				String name = key.replaceAll("plugin\\.", "");
				getPlugins().put(name, value);
			}
		}
	}
	
	public static Set<String> getAvailableSolvers() {
		return getPlugins().keySet();
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

	public static void setPlugins(HashMap<String, String> plugins) {
		QPar.plugins = plugins;
	}

	public static HashMap<String, String> getPlugins() {
		return plugins;
	}

	public static void setMailEvaluationResults(boolean mailEvaluationResults) {
		QPar.mailEvaluationResults = mailEvaluationResults;
	}

	public static boolean isMailEvaluationResults() {
		return mailEvaluationResults;
	}
	
}
