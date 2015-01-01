package net.sf.sitemonitoring.service;

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
			JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
			javaMailSenderImpl.setHost(configuration.getEmailServerHost());
			if (configuration.getEmailServerPort() != null) {
				javaMailSenderImpl.setPort(configuration.getEmailServerPort());
			}
			if (configuration.getEmailServerUsername() != null) {
				javaMailSenderImpl.setUsername(configuration.getEmailServerUsername());
			}
			if (configuration.getEmailServerPassword() != null) {
				javaMailSenderImpl.setPassword(configuration.getEmailServerPassword());
			}

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setFrom(configuration.getAdminEmail());
			mailMessage.setTo(configuration.getAdminEmail());
			mailMessage.setSubject(replaceTemplateValues(check, configuration.getEmailSubject(), checkResultText));
			mailMessage.setText(replaceTemplateValues(check, configuration.getEmailBody(), checkResultText));

			javaMailSenderImpl.send(mailMessage);
		} catch (Exception ex) {
			log.error("error sending email!", ex);
		}
	}

	private String replaceTemplateValues(Check check, String template, String errorText) {
		return template.replace("{CHECK-NAME}", check.getName()).replace("{CHECK-URL}", check.getUrl()).replace("{ERROR}", errorText);
	}
}
