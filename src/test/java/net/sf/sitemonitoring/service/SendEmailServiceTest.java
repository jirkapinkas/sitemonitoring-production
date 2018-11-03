package net.sf.sitemonitoring.service;

import static org.junit.Assert.*;

import net.sf.sitemonitoring.entity.Check;

import org.junit.Before;
import org.junit.Test;

public class SendEmailServiceTest {
	
	private SendEmailService sendEmailService;

	@Before
	public void setUp() {
		sendEmailService = new SendEmailService();
	}

	@Test
	public void testReplaceTemplateValues() {
		Check check = new Check();
		check.setName("check name");
		check.setUrl("check url");
		String template = "Check {CHECK-NAME} on URL {CHECK-URL} has errors: {ERROR}";
		String errorText = "test error";
		assertEquals("Check check name on URL check url has errors: test error", 
				sendEmailService.replaceTemplateValues(check, template, errorText));
	}

}
