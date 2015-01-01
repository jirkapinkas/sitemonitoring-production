package net.sf.sitemonitoring.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckCondition;
import net.sf.sitemonitoring.entity.Check.CheckType;
import net.sf.sitemonitoring.entity.Check.IntervalType;
import net.sf.sitemonitoring.entity.Configuration;
import net.sf.sitemonitoring.repository.CheckRepository;
import net.sf.sitemonitoring.repository.CheckResultRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class InitDbService {

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private CheckRepository checkRepository;

	@Autowired
	private CheckResultRepository checkResultRepository;

	@PostConstruct
	public void init() throws Exception {
		if (configurationService.find() != null) {
			// configuration already exists -> we're using production database
			return;
		}
		System.out.println("*** TEST DATABASE INIT STARTED ***");
		Configuration configuration = new Configuration();
		configuration.setEmailSubject("sitemonitoring error");
		configuration.setEmailBody("check name:{CHECK-NAME}\n\ncheck url: {CHECK-URL}\n\nerror:\n{ERROR}");
		configuration.setDefaultSingleCheckInterval(1);
		configuration.setDefaultSitemapCheckInterval(10);
		configuration.setSocketTimeout(20000);
		configuration.setConnectionTimeout(20000);
		configuration.setTooLongRunningCheckMinutes(30);
		configuration.setCheckBrokenLinks(false);
		configuration.setAdminUsername("admin");
		configuration.setAdminPassword(new BCryptPasswordEncoder().encode("admin"));
		configuration.setSendEmails(true);
		
		configurationService.save(configuration);

		{
			Check check = new Check();
			check.setName("check javavids homepage");
			check.setUrl("http://www.javavids.com/");
			check.setConditionType(CheckCondition.CONTAINS);
			check.setCondition("</html>");
			check.setType(CheckType.SINGLE_PAGE);
			check.setCheckBrokenLinks(true);
			check.setSocketTimeout(20000);
			check.setConnectionTimeout(20000);
			check.setScheduledInterval(1);
			check.setChartPeriodType(IntervalType.HOUR);
			check.setChartPeriodValue(1);
			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			check.setScheduledStartDate(calendar.getTime());
			check.setScheduledIntervalType(IntervalType.MINUTE);
			checkRepository.save(check);
		}

		{
			Check check = new Check();
			check.setName("check sqlvids sitemap");
			check.setUrl("http://www.sqlvids.com/sitemap.xml");
			check.setConditionType(CheckCondition.CONTAINS);
			check.setCondition("</html>");
			check.setType(CheckType.SITEMAP);
			check.setCheckBrokenLinks(true);
			check.setSocketTimeout(20000);
			check.setConnectionTimeout(20000);
			check.setScheduledInterval(10);
			check.setChartPeriodType(IntervalType.HOUR);
			check.setChartPeriodValue(1);
			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			check.setScheduledStartDate(calendar.getTime());
			check.setScheduledIntervalType(IntervalType.MINUTE);
			checkRepository.save(check);
		}

		{
			Check check = new Check();
			check.setName("check java skoleni sitemap");
			check.setUrl("http://localhost:8080/sitemap.xml");
			check.setConditionType(CheckCondition.CONTAINS);
			check.setCondition("</html>");
			check.setType(CheckType.SITEMAP);
			check.setExcludedUrls("*.pdf");
			check.setCheckBrokenLinks(true);
			check.setSocketTimeout(20000);
			check.setConnectionTimeout(20000);
			check.setScheduledInterval(10);
			check.setChartPeriodType(IntervalType.HOUR);
			check.setChartPeriodValue(1);
			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			check.setScheduledStartDate(calendar.getTime());
			check.setScheduledIntervalType(IntervalType.MINUTE);
			checkRepository.save(check);
		}
		System.out.println("*** TEST DATABASE INIT FINISHED ***");
	}

}
