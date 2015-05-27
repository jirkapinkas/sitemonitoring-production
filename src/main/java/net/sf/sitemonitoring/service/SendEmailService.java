package net.sf.sitemonitoring.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Configuration;
import net.sf.sitemonitoring.repository.CheckRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

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
		try {
			Configuration configuration = configurationService.find();
			if (!configuration.isSendEmails()) {
				return;
			}

			if (check.getCurrentErrorCount() < check.getMaxErrorCount()) {
				checkRepository.incErrorCount(check.getId());
				return;
			}

			checkRepository.sendEmail(check.getId());

			log.debug("send email");
			JavaMailSenderImpl javaMailSender = constructJavaMailSender(configuration);
			String from = configuration.getAdminEmail();
			String to = configuration.getAdminEmail();
			String subject = replaceTemplateValues(check, configuration.getEmailSubject(), checkResultText);
			String body = replaceTemplateValues(check, configuration.getEmailBody(), checkResultText);
			fixGmail(javaMailSender);
			sendEmail(javaMailSender, from, to, subject, body);
		} catch (Exception ex) {
			log.error("error sending email!", ex);
		}
	}

	private JavaMailSenderImpl constructJavaMailSender(Configuration configuration) {
		JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
		javaMailSenderImpl.setHost(configuration.getEmailServerHost());
		if (configuration.getEmailServerPort() != null) {
			javaMailSenderImpl.setPort(configuration.getEmailServerPort());
		}
		if (configuration.getEmailServerUsername() != null && !configuration.getEmailServerUsername().isEmpty()) {
			javaMailSenderImpl.setUsername(configuration.getEmailServerUsername());
		}
		if (configuration.getEmailServerPassword() != null && !configuration.getEmailServerPassword().isEmpty()) {
			javaMailSenderImpl.setPassword(configuration.getEmailServerPassword());
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

	public String sendEmailTest(String adminEmail, String emailServerHost, String emailServerPort, String emailServerUsername, String emailServerPassword) {
		log.debug("called sendEmailTest");
		try {
			String from = adminEmail;
			String to = adminEmail;
			String subject = "test subject";
			String body = "test body";
			JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
			javaMailSender.setHost(emailServerHost);
			if (emailServerPort != null && !emailServerPort.isEmpty()) {
				javaMailSender.setPort(Integer.parseInt(emailServerPort));
			}
			if (emailServerUsername != null && !emailServerUsername.isEmpty()) {
				javaMailSender.setUsername(emailServerUsername);
			}
			if (emailServerPassword != null && !emailServerPassword.isEmpty()) {
				javaMailSender.setPassword(emailServerPassword);
			}
			fixGmail(javaMailSender);
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

	private void fixGmail(JavaMailSenderImpl javaMailSender) {
		if("smtp.gmail.com".equals(javaMailSender.getHost()) && javaMailSender.getPort() == 465) {
			log.debug("gmail fix applied");
			Properties props = new Properties();
			props.put("mail.smtp.auth", true);
			props.put("mail.smtp.starttls.enable", false);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", false);
			props.put("mail.debug", true);
			javaMailSender.setJavaMailProperties(props);
			javaMailSender.setProtocol("smtp");
		}
	}

	private String replaceTemplateValues(Check check, String template, String errorText) {
		return template.replace("{CHECK-NAME}", check.getName()).replace("{CHECK-URL}", check.getUrl()).replace("{ERROR}", errorText);
	}

}
