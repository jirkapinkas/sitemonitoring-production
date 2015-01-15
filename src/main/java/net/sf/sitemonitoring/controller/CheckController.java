package net.sf.sitemonitoring.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckCondition;
import net.sf.sitemonitoring.entity.Check.CheckType;
import net.sf.sitemonitoring.entity.Check.IntervalType;
import net.sf.sitemonitoring.entity.Configuration;
import net.sf.sitemonitoring.service.CheckResultService;
import net.sf.sitemonitoring.service.CheckService;
import net.sf.sitemonitoring.service.ConfigurationService;

@Data
@ManagedBean
@ViewScoped
@Slf4j
public class CheckController implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManagedProperty("#{checkService}")
	private CheckService checkService;

	@ManagedProperty("#{checkResultService}")
	private CheckResultService checkResultService;

	@ManagedProperty("#{configurationService}")
	private ConfigurationService configurationService;

	private Check check;

	private Date originalScheduledStartDate;

	private int originalScheduledInterval;

	private List<Check> checks;

	@ManagedProperty("#{checkResultsController}")
	private CheckResultsController checkResultsController;
	
	@PostConstruct
	public void init() {
		log.debug("constructed CheckController");
		/*
		 * cyclic reference
		 */
		checkResultsController.setCheckController(this);
	}
	
	public Configuration getConfiguration() {
		return configurationService.find();
	}
	
	public void hideInfoMessage() {
		Configuration configuration = configurationService.find();
		configuration.setHideInfoMessage(true);
		configurationService.saveExcludingPassword(configuration);
	}

	public void abort(int checkId) {
		checkService.abort(checkId, "manual abort");
	}

	public void startNow(int checkId) {
		checkService.startNow(checkId);
	}

	public void setCheck(Check check) {
		this.check = check;
		this.originalScheduledInterval = check.getScheduledInterval();
		this.originalScheduledStartDate = check.getScheduledStartDate();
	}

	public void loadChecks() {
		checks = checkService.findAll();
		clearCheck();
	}

	public void updateResults() {
		checks = checkService.findAll();
		checkResultsController.updateResults();
	}

	public void clearCheck() {
		log.debug("clear check");
		check = new Check();
		check.setUrl("http://");
	}

	public void prepareSingleCheck() {
		log.debug("create single check");
		check = new Check(CheckType.SINGLE_PAGE);
		check.setScheduledInterval(configurationService.find().getDefaultSingleCheckInterval());
		prepareCheck();
	}

	public void prepareSitemapCheck() {
		log.debug("create sitemap check");
		check = new Check(CheckType.SITEMAP);
		check.setScheduledInterval(configurationService.find().getDefaultSitemapCheckInterval());
		check.setConditionType(CheckCondition.CONTAINS);
		prepareCheck();
	}

	public void prepareSpiderCheck() {
		log.debug("create spider check");
		check = new Check(CheckType.SPIDER);
		check.setScheduledInterval(configurationService.find().getDefaultSpiderCheckInterval());
		check.setConditionType(CheckCondition.CONTAINS);
		prepareCheck();
	}

	private void prepareCheck() {
		check.setUrl("http://");
		Configuration configuration = configurationService.find();
		check.setCheckBrokenLinks(configuration.isCheckBrokenLinks());
		check.setSocketTimeout(configuration.getSocketTimeout());
		check.setConnectionTimeout(configuration.getConnectionTimeout());
		check.setScheduledIntervalType(IntervalType.MINUTE);
		check.setSendEmails(configuration.getDefaultSendEmails());
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		check.setScheduledStartDate(calendar.getTime());
		check.setChartPeriodType(IntervalType.HOUR);
		check.setChartPeriodValue(3);
	}

	public void save() {
		log.debug("save check: " + check.getType());
		if (check.getScheduledInterval() != originalScheduledInterval || !check.getScheduledStartDate().equals(originalScheduledStartDate)) {
			log.debug("interval or start date has changed");
			log.debug("intervals: new:" + check.getScheduledInterval() + ", old: " + originalScheduledInterval);
			log.debug("start dates: new:" + check.getScheduledStartDate() + ", old: " + originalScheduledStartDate);

			check.setScheduledNextDate(null);
		}
		checkService.save(check);
		clearCheck();
		checkResultsController.loadChecks();
		updateResults();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Check saved"));
	}

	public void delete(int id) {
		checkService.delete(id);
		loadChecks();
	}

}
