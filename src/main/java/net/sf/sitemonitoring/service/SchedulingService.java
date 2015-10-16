package net.sf.sitemonitoring.service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckState;
import net.sf.sitemonitoring.service.check.MonitoringService;

import org.primefaces.push.EventBusFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

@Service
@Slf4j
public class SchedulingService {

	@Autowired
	private MonitoringService monitoringService;

	@Autowired
	private CheckService checkService;

	@Autowired
	private CheckResultService checkResultService;

	@Autowired
	private EventBus eventBus;

	@Autowired
	private ConfigurationService configurationService;

	/**
	 * Schedule monitoring
	 */
	@Scheduled(fixedDelay = 3000)
	public void runMonitoring() {
		if (EventBusFactory.getDefault() == null) {
			// Atmosphere is not yet initialized (this can happen because it's a
			// servlet and it's initialized after Spring listener.
			return;
		}
		log.debug("start run monitoring");
		List<Check> checks = checkService.findAll();
		for (Check check : checks) {
			if (check.getCheckState() == CheckState.NOT_RUNNING) {
				boolean startCheck = false;
				Date scheduledNextDate = check.getScheduledNextDate();
				if (scheduledNextDate == null) {
					if (new Date().after(check.getScheduledStartDate())) {
						startCheck = true;
					}
				} else {
					if (new Date().after(scheduledNextDate)) {
						startCheck = true;
					}
				}

				if (startCheck) {
					Calendar calendar = new GregorianCalendar();
					switch (check.getScheduledIntervalType()) {
					case SECOND:
						calendar.add(Calendar.SECOND, check.getScheduledInterval());
						break;
					case MINUTE:
						calendar.add(Calendar.MINUTE, check.getScheduledInterval());
						break;
					case HOUR:
						calendar.add(Calendar.HOUR_OF_DAY, check.getScheduledInterval());
						break;
					case DAY:
						calendar.add(Calendar.DAY_OF_MONTH, check.getScheduledInterval());
						break;
					case MONTH:
						calendar.add(Calendar.MONTH, check.getScheduledInterval());
						break;
					default:
						throw new UnsupportedOperationException("Unknown scheduled interval type");
					}
					monitoringService.startCheck(check, calendar.getTime());
				}
			}
		}
		log.debug("finish run monitoring");
	}

	/**
	 * Each hour delete old statistics
	 * 
	 */
	@Scheduled(fixedDelay = 5000)
	public void deleteHistoricData() {
		log.debug("start delete historic data");
		List<Check> checks = checkService.findAll();
		for (Check check : checks) {
			GregorianCalendar calendar = new GregorianCalendar();
			switch (check.getKeepResultType()) {
			case MONTH:
				calendar.add(Calendar.MONTH, -check.getKeepResultsValue());
				break;
			case DAY:
				calendar.add(Calendar.DATE, -check.getKeepResultsValue());
				break;
			case HOUR:
				calendar.add(Calendar.HOUR, -check.getKeepResultsValue());
				break;
			case MINUTE:
				calendar.add(Calendar.MINUTE, -check.getKeepResultsValue());
				break;
			case SECOND:
				calendar.add(Calendar.SECOND, -check.getKeepResultsValue());
				break;
			default:
				throw new UnsupportedOperationException("Unknown keep results type");
			}
			checkResultService.deleteOld(check.getId(), calendar.getTime());
		}
		log.debug("finish delete historic data");
	}

	/**
	 * Delete too long running checks
	 */
	@Scheduled(fixedDelay = 10000)
	public void deleteLongRunningChecks() {
		List<Check> checks = checkService.findAll();
		for (Check check : checks) {
			if (check.getCheckState() == CheckState.RUNNING) {
				Calendar calendar = new GregorianCalendar();
				calendar.add(Calendar.MINUTE, -configurationService.find().getTooLongRunningCheckMinutes());
				Date dateTooOld = calendar.getTime();
				if (check.getStartDate().before(dateTooOld)) {
					checkService.abort(check.getId(), "check too long");
				}

			}
		}
	}
	
	public void setCheckService(CheckService checkService) {
		this.checkService = checkService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setCheckResultService(CheckResultService checkResultService) {
		this.checkResultService = checkResultService;
	}
}
