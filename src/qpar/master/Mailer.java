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

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.sun.mail.smtp.SMTPTransport;

public class Mailer {
	static Logger logger = Logger.getLogger(Mailer.class);
	
	public static String email	= null;
	public static String server = null;
	public static String user 	= null;
	public static String pass 	= null;
	
	public static void send_mail(String email, String server, String user,
			String pass, String subject, String message) {
		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.smtp.port", 587);
		Session session = Session.getInstance(props, null);
		Message msg = new MimeMessage(session);
		try {
			msg.setFrom();
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(
					email, false));
			msg.setSubject(subject);
			msg.setText(message);
			msg.setHeader("X-Mailer", "smtpsend");
			msg.setSentDate(new Date());
			SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
			t.connect(server, user, pass);
			t.sendMessage(msg, msg.getAllRecipients());
			t.close();
		} catch (MessagingException e) {
			logger.error("TO: " + email + ", SERVER: " + server + ", USER: " + user, e);
		}
	}
}
