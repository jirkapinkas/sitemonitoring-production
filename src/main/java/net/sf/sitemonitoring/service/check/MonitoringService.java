package net.sf.sitemonitoring.service.check;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.CheckResult;
import net.sf.sitemonitoring.repository.CheckResultRepository;
import net.sf.sitemonitoring.service.CheckResultService;
import net.sf.sitemonitoring.service.CheckService;
import net.sf.sitemonitoring.service.SendEmailService;

import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitoringService {

	@Autowired
	private SinglePageCheckService singleCheckService;

	@Autowired
	private SitemapCheckService sitemapCheckService;

	@Autowired
	private SpiderCheckService spiderCheckService;

	@Autowired
	private CheckResultRepository checkResultRepository;

	@Autowired
	private CheckResultService checkResultService;

	@Autowired
	private CheckService checkService;

	@Autowired
	private SendEmailService sendEmailService;

	@Async
	public void startCheck(Check check, Date scheduledNextDate) {
		log.debug("check monitoring started: " + check.getName());
		check = checkService.startCheck(check, scheduledNextDate);
		monitoringStarted();
		CheckResult checkResult = new CheckResult();
		checkResult.setCheck(check);
		checkResult.setStartTime(new Date());

		long startTime = System.nanoTime();
		String checkResultText = null;

		switch (check.getType()) {
		case SINGLE_PAGE:
			checkResultText = singleCheckService.performCheck(check);
			break;
		case SITEMAP:
			checkResultText = sitemapCheckService.performCheck(check);
			break;
		case SPIDER:
			checkResultText = spiderCheckService.performCheck(check);
			break;
		default:
			throw new UnsupportedOperationException("this check type is not supported!");
		}

		checkResult.setFinishTime(new Date());
		// time in milliseconds
		checkResult.setResponseTime((System.nanoTime() - startTime) / 1000000);
		if (checkResultText != null) {
			checkResult.setDescription(checkResultText);
			checkResult.setSuccess(false);
			if (check.isSendEmails()) {
				sendEmailService.sendEmail(check, checkResultText);
			}
		} else {
			checkResult.setSuccess(true);
			sendEmailService.clearErrorCount(check.getId());
		}
		checkResultRepository.save(checkResult);

		checkService.finishCheck(check);
		monitoringFinished();
		log.debug("check monitoring finished: " + check.getName());
	}

	public void monitoringStarted() {
		EventBus eventBus = EventBusFactory.getDefault().eventBus();
		eventBus.publish("/running", true);
		log.debug("client notified about monitoring startup");
	}

	public void monitoringFinished() {
		EventBus eventBus = EventBusFactory.getDefault().eventBus();
		eventBus.publish("/running", false);
		log.debug("client notified about monitoring finish");
	}
}
