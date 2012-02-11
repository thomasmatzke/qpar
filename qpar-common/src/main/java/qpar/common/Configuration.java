/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package qpar.common;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

public class Configuration {
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(Configuration.class);

	public static final String EXCEPTION_NOTIFICATION = "enableExceptionNotifications";
	public static final String NOTIFICATION_ADDRESS = "exceptionNotifierAddress";
	public static final String AVAILABLE_PROCESSORS = "availableProcessors";
	public static final String MAIL_EVAL_RESULTS = "mailEvaluationResults";
	public static final String EVAL_RESULT_ADDRESS = "evaluationAddress";
	public static final String MAIL_SRV = "mailServer";
	public static final String MAIL_USR = "mailUser";
	public static final String MAIL_PW = "mailPass";
	public static final String BENCHMARK_MODE = "benchmarkMode";
	public static final String RESULT_CACHING = "resultCaching";
	public static final String SCHEDULING_QUEUE_SIZE = "scheduling.queue_size";

	private Properties prop = null;

	public Configuration(final Properties prop) {
		this.prop = prop;
	}

	public <T> T getProperty(final String key, final Class<T> type) {
		if (type == Boolean.class) {
			return type.cast(Boolean.parseBoolean(this.prop.getProperty(key)));
		} else if (type == String.class) {
			return type.cast(this.prop.getProperty(key));
		} else if (type == Integer.class) {
			return type.cast(Integer.parseInt(this.prop.getProperty(key)));
		} else {
			throw new IllegalArgumentException("Can only parse Strings, Booleans and Integers from configuration file");
		}
	}

	public Boolean isValid() {
		if (this.getProperty(Configuration.EXCEPTION_NOTIFICATION, Boolean.class)
				&& (this.prop.getProperty(Configuration.NOTIFICATION_ADDRESS) == null || !this.isMailInformationComplete())) {
			return false;
		}

		if (this.getProperty(Configuration.MAIL_EVAL_RESULTS, Boolean.class)
				&& (this.prop.getProperty(Configuration.EVAL_RESULT_ADDRESS) == null || !this.isMailInformationComplete())) {
			return false;
		}

		return true;
	}

	private Boolean isMailInformationComplete() {
		return this.getProperty(Configuration.MAIL_PW, Boolean.class) && this.getProperty(Configuration.MAIL_SRV, Boolean.class)
				&& this.getProperty(Configuration.MAIL_USR, Boolean.class);
	}

	public HashMap<String, String> getPlugins() {
		HashMap<String, String> plugins = new HashMap<String, String>();

		for (Entry<Object, Object> e : this.prop.entrySet()) {
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			if (key.startsWith("plugin.")) {
				String name = key.replaceAll("plugin\\.", "");
				plugins.put(name, value);
			}
		}

		return plugins;
	}

	public HashMap<String, String> getHeuristics() {
		HashMap<String, String> heuristics = new HashMap<String, String>();

		for (Entry<Object, Object> e : this.prop.entrySet()) {
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			if (key.startsWith("heuristic.")) {
				String name = key.replaceAll("heuristic\\.", "");
				heuristics.put(name, value);
			}
		}

		return heuristics;
	}

	// private static boolean enableExceptionNotifications = false;
	// private static String exceptionNotifierAddress = null;
	// private static String mailServer = null;
	// private static String mailUser = null;
	// private static String mailPass = null;
	// private static boolean benchmarkMode = false;
	// private static boolean resultCaching = false;
	// private static boolean mailEvaluationResults = false;
	// private static String evaluationAddress = null;
	// private static Integer availableProcessors = null;
	// private static HashMap<String, String> plugins = new HashMap<String,
	// String>();
	//
	// public static boolean isResultCaching() {
	// return resultCaching;
	// }
	//
	// public static void setResultCaching(final boolean resultCaching) {
	// Configuration.resultCaching = resultCaching;
	// }
	//
	// public static boolean isMailInfoComplete() {
	// return mailServer != null && mailUser != null && mailPass != null;
	// }
	//
	// public static void loadConfig() {
	// Properties properties = new Properties();
	// try {
	// properties.load(new FileInputStream("qpar.conf"));
	// } catch (IOException e) {
	// LOGGER.warn("Config file not found.");
	// }
	// Configuration.mailPass = properties.getProperty("mailPass");
	// Configuration.mailUser = properties.getProperty("mailUser");
	// Configuration.mailServer = properties.getProperty("mailServer");
	// Configuration.setExceptionNotifierAddress(properties.getProperty("exceptionNotifierAddress"));
	// Configuration.setEvaluationAddress(properties.getProperty("evaluationAddress"));
	// Configuration.enableExceptionNotifications =
	// Boolean.parseBoolean(properties.getProperty("enableExceptionNotifications"));
	// Configuration.setBenchmarkMode(Boolean.parseBoolean(properties.getProperty("benchmarkMode")));
	// Configuration.setMailEvaluationResults(Boolean.parseBoolean(properties.getProperty("mailEvaluationResults")));
	// Configuration.setResultCaching(Boolean.parseBoolean(properties.getProperty("resultCaching")));
	// Integer processors =
	// Integer.parseInt(properties.getProperty("availableProcessors"));
	// if (processors == 0) {
	// Configuration.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
	// } else {
	// Configuration.setAvailableProcessors(processors);
	// }
	//
	// for (Entry<Object, Object> e : properties.entrySet()) {
	// String key = (String) e.getKey();
	// String value = (String) e.getValue();
	// if (key.startsWith("plugin.")) {
	// String name = key.replaceAll("plugin\\.", "");
	// getPlugins().put(name, value);
	// }
	// }
	// }
	//
	// public static Set<String> getAvailableSolvers() {
	// return getPlugins().keySet();
	// }
	//
	// public static void sendExceptionMail(final Throwable t) {
	// if (!Configuration.enableExceptionNotifications) {
	// return;
	// }
	// String body = "";
	// try {
	// body += "Host: " + InetAddress.getLocalHost().getHostName() + "\n";
	// } catch (UnknownHostException e) {
	// body += "Host: UNKNOWN (Exception ocurred)\n";
	// }
	// body += StackTraceUtil.getStackTrace(t);
	// if (Configuration.isMailInfoComplete() &&
	// Configuration.getExceptionNotifierAddress() != null) {
	// Mailer.send(Configuration.getExceptionNotifierAddress(),
	// Configuration.mailServer, Configuration.mailUser,
	// Configuration.mailPass, "Exception Notification", body);
	// }
	// }
	//
	// public static String getMailServer() {
	// return mailServer;
	// }
	//
	// public static void setMailServer(final String mailServer) {
	// Configuration.mailServer = mailServer;
	// }
	//
	// public static String getMailUser() {
	// return mailUser;
	// }
	//
	// public static void setMailUser(final String mailUser) {
	// Configuration.mailUser = mailUser;
	// }
	//
	// public static String getMailPass() {
	// return mailPass;
	// }
	//
	// public static void setMailPass(final String mailPass) {
	// Configuration.mailPass = mailPass;
	// }
	//
	// public static void setExceptionNotifierAddress(final String
	// exceptionNotifierAddress) {
	// Configuration.exceptionNotifierAddress = exceptionNotifierAddress;
	// }
	//
	// public static String getExceptionNotifierAddress() {
	// return exceptionNotifierAddress;
	// }
	//
	// public static void setBenchmarkMode(final boolean benchmarkMode) {
	// Configuration.benchmarkMode = benchmarkMode;
	// }
	//
	// public static boolean isBenchmarkMode() {
	// return benchmarkMode;
	// }
	//
	// public static void setPlugins(final HashMap<String, String> plugins) {
	// Configuration.plugins = plugins;
	// }
	//
	// public static HashMap<String, String> getPlugins() {
	// return plugins;
	// }
	//
	// public static void setMailEvaluationResults(final boolean
	// mailEvaluationResults) {
	// Configuration.mailEvaluationResults = mailEvaluationResults;
	// }
	//
	// public static boolean isMailEvaluationResults() {
	// return mailEvaluationResults;
	// }
	//
	// public static void setEvaluationAddress(final String evaluationAddress) {
	// Configuration.evaluationAddress = evaluationAddress;
	// }
	//
	// public static String getEvaluationAddress() {
	// return evaluationAddress;
	// }
	//
	// public static void setAvailableProcessors(final Integer
	// availableProcessors) {
	// Configuration.availableProcessors = availableProcessors;
	// }
	//
	// public static Integer getAvailableProcessors() {
	// return availableProcessors;
	// }

}
