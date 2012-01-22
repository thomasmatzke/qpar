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
package qpar.master;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.Configuration;
import qpar.common.StackTraceUtil;

import com.sun.mail.smtp.SMTPTransport;

public class Mailer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Mailer.class);

	public static String email = null;
	public static String server = null;
	public static String user = null;
	public static String pass = null;

	public static void send(final String email, final String server, final String user, final String pass, final String subject,
			final String message) {
		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.smtp.port", 587);
		Session session = Session.getInstance(props, null);
		Message msg = new MimeMessage(session);
		try {
			msg.setFrom();
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
			msg.setSubject(subject);
			msg.setText(message);
			msg.setHeader("X-Mailer", "smtpsend");
			msg.setSentDate(new Date());
			SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
			t.connect(server, user, pass);
			t.sendMessage(msg, msg.getAllRecipients());
			t.close();
		} catch (MessagingException e) {
			LOGGER.error("TO: " + email + ", SERVER: " + server + ", USER: " + user, e);
		}
	}

	public static void sendExceptionMail(final Throwable t) {
		if (!Master.configuration.getProperty(Configuration.EXCEPTION_NOTIFICATION, Boolean.class)) {
			return;
		}
		String body = "";
		try {
			body += "Host: " + InetAddress.getLocalHost().getHostName() + "\n";
		} catch (UnknownHostException e) {
			body += "Host: UNKNOWN (Exception ocurred)\n";
		}
		body += StackTraceUtil.getStackTrace(t);
		if (Master.configuration.isValid()) {
			Mailer.send(Master.configuration.getProperty(Configuration.NOTIFICATION_ADDRESS, String.class),
					Master.configuration.getProperty(Configuration.MAIL_SRV, String.class),
					Master.configuration.getProperty(Configuration.MAIL_USR, String.class),
					Master.configuration.getProperty(Configuration.MAIL_PW, String.class), "Exception Notification", body);
		}
	}
}
