package net.sf.sitemonitoring.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.sun.mail.util.MailSSLSocketFactory;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Configuration;
import net.sf.sitemonitoring.repository.CheckRepository;

@Slf4j
@Service
public class SendEmailService {

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private CheckRepository checkRepository;

	public void clearErrorCount(int checkId) {
		checkRepository.clearErrorCount(checkId);
	}

	public void sendEmail(Check check, String checkResultText) {
		log.debug("sending email ...");
		try {
			Configuration configuration = configurationService.find();
			if (!configuration.isSendEmails()) {
				return;
			}

			if (check.getCurrentErrorCount() < check.getMaxErrorCount()) {
				checkRepository.incErrorCount(check.getId());
				return;
			}

			JavaMailSenderImpl javaMailSender = constructJavaMailSender(configuration.getEmailServerHost(), configuration.getEmailServerPort(), configuration.getEmailServerUsername(),
					configuration.getEmailServerPassword());
			String from = configuration.getEmailFrom();
			String to = configuration.getAdminEmail();
			String subject = replaceTemplateValues(check, configuration.getEmailSubject(), checkResultText);
			String body = replaceTemplateValues(check, configuration.getEmailBody(), checkResultText);
			applyFixes(javaMailSender);
			sendEmail(javaMailSender, from, to, subject, body);
			checkRepository.emailSent(check.getId());
			log.debug("... email sent");
		} catch (Exception ex) {
			log.error("error sending email!", ex);
		}
	}

	private JavaMailSenderImpl constructJavaMailSender(String emailServerHost, Integer emailServerPort, String emailServerUsername, String emailServerPassword) {
		JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
		javaMailSenderImpl.setHost(emailServerHost);
		if (emailServerPort != null) {
			javaMailSenderImpl.setPort(emailServerPort);
		}
		if (emailServerUsername != null && !emailServerUsername.isEmpty()) {
			javaMailSenderImpl.setUsername(emailServerUsername);
		}
		if (emailServerPassword != null && !emailServerPassword.isEmpty()) {
			javaMailSenderImpl.setPassword(emailServerPassword);
		}
		return javaMailSenderImpl;
	}
	
	private void sendEmail(JavaMailSenderImpl javaMailSenderImpl, String from, String to, String subject, String body) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(from);
		mailMessage.setTo(to);
		mailMessage.setSubject(subject);
		mailMessage.setText(body);
		javaMailSenderImpl.send(mailMessage);
	}

	public String sendEmailTest(String emailFrom, String adminEmail, String emailServerHost, String emailServerPort, String emailServerUsername, String emailServerPassword) {
		log.debug("called sendEmailTest");
		try {
			String from = emailFrom;
			String to = adminEmail;
			String subject = "test subject";
			String body = "test body";
			JavaMailSenderImpl javaMailSender = constructJavaMailSender(emailServerHost, Integer.parseInt(emailServerPort), emailServerUsername, emailServerPassword);
			applyFixes(javaMailSender);
			sendEmail(javaMailSender, from, to, subject, body);
			return "all ok";
		} catch (Exception ex) {
			ex.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			return sw.toString(); // stack trace as a string
		}
	}

	private void applyFixes(JavaMailSenderImpl javaMailSender) throws GeneralSecurityException {
		Properties props = new Properties();
		fixAuth(javaMailSender, props);
		fixSSL(props);
		if (props.size() > 0) {
			javaMailSender.setJavaMailProperties(props);
		}
	}

	private void fixSSL(Properties props) throws GeneralSecurityException {
		MailSSLSocketFactory mailSSLSocketFactory;
		mailSSLSocketFactory = new MailSSLSocketFactory();
		mailSSLSocketFactory.setTrustAllHosts(true);
		props.put("mail.smtp.ssl.socketFactory", mailSSLSocketFactory);
	}

	private void fixAuth(JavaMailSenderImpl javaMailSender, Properties props) {
		if (javaMailSender.getPort() == 465 || javaMailSender.getPort() == 587) {
		    // for two step authentication
		    // see : https://support.google.com/accounts/answer/185833
			props.put("mail.smtp.auth", true);
			props.put("mail.smtp.starttls.enable", true);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", false);
			props.put("mail.debug", true);
			javaMailSender.setProtocol("smtp");
		}
	}

	String replaceTemplateValues(Check check, String template, String errorText) {
		return template.replace("{CHECK-NAME}", check.getName()).replace("{CHECK-URL}", check.getUrl()).replace("{ERROR}", errorText);
	}

}
