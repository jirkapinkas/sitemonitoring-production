package net.sf.sitemonitoring.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import lombok.Data;
import net.sf.sitemonitoring.annotation.ScopeView;
import net.sf.sitemonitoring.entity.Configuration;
import net.sf.sitemonitoring.service.ConfigurationService;
import net.sf.sitemonitoring.service.SendEmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@ScopeView
public class ConfigurationController implements Serializable {

	private static final long serialVersionUID = 1L;

	private Configuration configuration;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private SendEmailService sendEmailService;

	private String emailTestResults;

	@PostConstruct
	public void loadConfiguration() {
		configurationService.clearCacheConfiguration();
		configuration = configurationService.find();
	}

	public void save() {
		configurationService.save(configuration);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Configuration saved"));
	}

	public void testSendEmails(String emailFrom, String adminEmail, String emailServerHost, String emailServerPort, String emailServerUsername, String emailServerPassword) {
		emailTestResults = "<h2>Email test results:</h2>\n\n" + sendEmailService.sendEmailTest(emailFrom, adminEmail, emailServerHost, emailServerPort, emailServerUsername, emailServerPassword);
	}

	public void loadDefaultsLocalhost() {
		configuration.setEmailServerHost("localhost");
		configuration.setEmailServerPort(25);
		configuration.setEmailServerUsername(null);
		configuration.setEmailServerPassword(null);
	}

	public void loadDefaultsGmail() {
		configuration.setEmailServerHost("smtp.gmail.com");
		configuration.setEmailServerPort(465);
		configuration.setEmailServerUsername("YOUR@GMAIL.COM");
		configuration.setEmailServerPassword("YOUR_PASSWORD");
	}

	public void loadDefaultsMandrillapp() {
		configuration.setEmailServerHost("smtp.mandrillapp.com");
		configuration.setEmailServerPort(587);
		configuration.setEmailServerUsername("YOUR_MANDRILLAPP_USERNAME");
		configuration.setEmailServerPassword("MANDRILLAPP_GENERATED_KEY");
	}

}
