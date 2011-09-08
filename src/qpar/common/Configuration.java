package qpar.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import qpar.master.Mailer;

public class Configuration {
	static Logger logger = Logger.getLogger(Configuration.class);
	
	private static boolean enableExceptionNotifications = false;
	private static String exceptionNotifierAddress = null;
	private static String mailServer = null;
	private static String mailUser = null;
	private static String mailPass = null;
	private static boolean benchmarkMode = false;
	private static boolean resultCaching = false;
	private static boolean mailEvaluationResults = false;
	private static String evaluationAddress = null;
	private static Integer availableProcessors = null;
	private static HashMap<String, String> plugins = new HashMap<String, String>();
	
	public static boolean isResultCaching() {
		return resultCaching;
	}

	public static void setResultCaching(boolean resultCaching) {
		Configuration.resultCaching = resultCaching;
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
		Configuration.mailPass 						= properties.getProperty("mailPass");
		Configuration.mailUser 						= properties.getProperty("mailUser");
		Configuration.mailServer 					= properties.getProperty("mailServer");
		Configuration.setExceptionNotifierAddress(properties.getProperty("exceptionNotifierAddress"));
		Configuration.setEvaluationAddress(properties.getProperty("evaluationAddress"));
		Configuration.enableExceptionNotifications 	= Boolean.parseBoolean(properties.getProperty("enableExceptionNotifications"));
		Configuration.setBenchmarkMode(Boolean.parseBoolean(properties.getProperty("benchmarkMode")));
		Configuration.setMailEvaluationResults(Boolean.parseBoolean(properties.getProperty("mailEvaluationResults")));
		Configuration.setResultCaching(Boolean.parseBoolean(properties.getProperty("resultCaching")));
		Integer processors = Integer.parseInt(properties.getProperty("availableProcessors"));
		if(processors == 0) {
			Configuration.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
		} else {
			Configuration.setAvailableProcessors(processors);
		}
		
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
		if(!Configuration.enableExceptionNotifications)
			return;
		String body = "";
		try {
			body += "Host: " + InetAddress.getLocalHost().getHostName() + "\n";
		} catch (UnknownHostException e) {
			body += "Host: UNKNOWN (Exception ocurred)\n";
		}
		body += StackTraceUtil.getStackTrace(t);
		if(Configuration.isMailInfoComplete() && Configuration.getExceptionNotifierAddress() != null)
			Mailer.send_mail(Configuration.getExceptionNotifierAddress(), Configuration.mailServer, Configuration.mailUser, Configuration.mailPass, "Exception Notification", body);
	}

	public static String getMailServer() {
		return mailServer;
	}

	public static void setMailServer(String mailServer) {
		Configuration.mailServer = mailServer;
	}

	public static String getMailUser() {
		return mailUser;
	}

	public static void setMailUser(String mailUser) {
		Configuration.mailUser = mailUser;
	}

	public static String getMailPass() {
		return mailPass;
	}

	public static void setMailPass(String mailPass) {
		Configuration.mailPass = mailPass;
	}

	public static void setExceptionNotifierAddress(String exceptionNotifierAddress) {
		Configuration.exceptionNotifierAddress = exceptionNotifierAddress;
	}

	public static String getExceptionNotifierAddress() {
		return exceptionNotifierAddress;
	}

	public static void setBenchmarkMode(boolean benchmarkMode) {
		Configuration.benchmarkMode = benchmarkMode;
	}

	public static boolean isBenchmarkMode() {
		return benchmarkMode;
	}

	public static void setPlugins(HashMap<String, String> plugins) {
		Configuration.plugins = plugins;
	}

	public static HashMap<String, String> getPlugins() {
		return plugins;
	}

	public static void setMailEvaluationResults(boolean mailEvaluationResults) {
		Configuration.mailEvaluationResults = mailEvaluationResults;
	}

	public static boolean isMailEvaluationResults() {
		return mailEvaluationResults;
	}

	public static void setEvaluationAddress(String evaluationAddress) {
		Configuration.evaluationAddress = evaluationAddress;
	}

	public static String getEvaluationAddress() {
		return evaluationAddress;
	}

	public static void setAvailableProcessors(Integer availableProcessors) {
		Configuration.availableProcessors = availableProcessors;
	}

	public static Integer getAvailableProcessors() {
		return availableProcessors;
	}
	
}
