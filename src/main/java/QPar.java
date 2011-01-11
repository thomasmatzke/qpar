package main.java;

import main.java.master.Mailer;

import org.apache.log4j.Level;

public class QPar {
	public static Level logLevel = Level.ERROR;
	public static String exceptionNotifierAddress = null;
	public static String mailServer = null;
	public static String mailUser = null;
	public static String mailPass = null;
	
	
	public static boolean isMailInfoComplete() {
		return mailServer != null && mailUser != null && mailPass != null;
	}
	
	
}
