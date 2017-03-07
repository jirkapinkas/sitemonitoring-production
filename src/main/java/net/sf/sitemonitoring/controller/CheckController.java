package net.sf.sitemonitoring.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.annotation.ScopeView;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckCondition;
import net.sf.sitemonitoring.entity.Check.CheckType;
import net.sf.sitemonitoring.entity.Check.HttpMethod;
import net.sf.sitemonitoring.entity.Check.IntervalType;
import net.sf.sitemonitoring.entity.Configuration;
import net.sf.sitemonitoring.entity.Credentials;
import net.sf.sitemonitoring.service.CheckResultService;
import net.sf.sitemonitoring.service.CheckService;
import net.sf.sitemonitoring.service.ConfigurationService;
import net.sf.sitemonitoring.service.PageService;

@Data
@Component
@ScopeView
@Slf4j
public class CheckController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
	private CheckService checkService;

	@Autowired
	private CheckResultService checkResultService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private PageService pageService;

	@Autowired
	private PageController pageController;

	private Check check;

	private List<Check> checks;

	@Autowired
	private CheckResultsController checkResultsController;

	@Autowired
	private PageSelectionController pageSelectionController;

	private Integer pageId;

	@PostConstruct
	public void init() {
		log.debug("constructed CheckController");
		/*
		 * cyclic reference
		 */
		checkResultsController.setCheckController(this);
	}

	private boolean removeCredentialsAfterSave;

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
		if (check.getPage() != null) {
			int checkId = -1;
			if (check.getPage() != null) {
				checkId = check.getPage().getId();
			}
			pageSelectionController.setSelectedPage(checkId);
		}
		this.check = check;
	}

	public void loadChecksForPage(Integer pageId) {
		this.pageId = pageId;
		loadChecks();
	}

	public void loadChecks() {
		checks = checkService.findByPageId(pageId);
		clearCheck();
	}

	public void updateResults() {
		loadChecks();
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

	public void prepareXmlCheck() {
		log.debug("create xml check");
		check = new Check(CheckType.XML);
		check.setScheduledInterval(configurationService.find().getDefaultSingleCheckInterval());
		check.setConditionType(CheckCondition.CONTAINS);
		prepareCheck();
	}

	public void prepareXsdCheck() {
		log.debug("create xsd check");
		check = new Check(CheckType.XSD);
		check.setScheduledInterval(configurationService.find().getDefaultSingleCheckInterval());
		check.setConditionType(CheckCondition.CONTAINS);
		prepareCheck();
	}

	public void prepareJsonCheck() {
		log.debug("create json check");
		check = new Check(CheckType.JSON);
		check.setScheduledInterval(configurationService.find().getDefaultSingleCheckInterval());
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
		if (pageId != null && pageId > 0) {
			pageSelectionController.setSelectedPage(pageId);
		}
		check.setCheckForChanges(configuration.isCheckForChanges());
		check.setCheckForChangesFilter(configuration.getCheckForChangesFilter());
	}

	public void save() {
		log.debug("save check: " + check.getType());
		if ((check.getCondition() != null && !check.getCondition().isEmpty()) || check.isCheckBrokenLinks() || check.isCheckForChanges()) {
			check.setHttpMethod(HttpMethod.GET);
		} else {
			check.setHttpMethod(HttpMethod.HEAD);
		}
		if (pageSelectionController.getSelectedPage() > -1) {
			check.setPage(pageService.findOne(pageSelectionController.getSelectedPage()));
		} else {
			check.setPage(null);
		}
		checkService.save(check);
		if (removeCredentialsAfterSave) {
			checkService.removeCredentials(check.getCredentials().getId());
		}
		clearCheck();
		checkResultsController.loadChecks();
		updateResults();
		pageController.loadPages();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Check saved"));
	}

	public void delete(int id) {
		checkService.delete(id);
		loadChecks();
	}
	
	public void deleteAll() {
		checkService.deleteAll();
		loadChecks();
	}

	public void addCredentials() {
		if (check.getCredentials() == null) {
			Credentials credentials = new Credentials();
			check.setCredentials(credentials);
		}
		removeCredentialsAfterSave = false;
	}

	public void removeCredentials() {
		removeCredentialsAfterSave = true;
	}

	public void clearResults(int checkId) {
		// delete check results from database
		checkResultService.deleteResults(checkId);
		// clears chart and table results
		checkResultsController.clearAllResults(checkId);
		// if user is displaying chart, we need to reload it, otherwise
		// exception is thrown
		checkResultsController.loadChartResults(checkId);
		// if user is displaying last results, we need to reload it
		checkResultsController.getLastResults().remove(checkId);
	}

}
