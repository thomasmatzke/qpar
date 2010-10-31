package main.java.master;

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
			logger.error(e);
		}
	}
}
