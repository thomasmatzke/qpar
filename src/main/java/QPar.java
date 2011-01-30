package main.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
	
}
