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
	
	@Autowired
	private UpgradeService upgradeService;

	@PostConstruct
	public void init() throws Exception {
		Configuration configuration = configurationService.find();
		if (configuration != null) {
			// configuration already exists -> we're using production database
			upgradeService.upgradeDatabase(configuration);
			return;
		}
		System.out.println("*** TEST DATABASE INIT STARTED ***");
		configuration = new Configuration();
		configuration.setMonitoringVersion("2.1.2");
		configuration.setEmailSubject("sitemonitoring error");
		configuration.setEmailBody("check name:{CHECK-NAME}\n\ncheck url: {CHECK-URL}\n\nerror:\n{ERROR}");
		configuration.setDefaultSingleCheckInterval(5);
		configuration.setDefaultSitemapCheckInterval(30);
		configuration.setDefaultSpiderCheckInterval(60);
		configuration.setDefaultSendEmails(false);
		configuration.setSocketTimeout(20000);
		configuration.setConnectionTimeout(20000);
		configuration.setTooLongRunningCheckMinutes(30);
		configuration.setCheckBrokenLinks(false);
		configuration.setAdminUsername("admin");
		configuration.setAdminPassword(new BCryptPasswordEncoder().encode("admin"));
		configuration.setSendEmails(true);
		configuration.setInfoMessage("Please don't monitor my websites (like javavids.com and sitemonitoring.souceforge.net). Lot's of people started doing it and effectively DDOSed them. If you monitor them anyway, your IP address will be blocked!");
		
		configurationService.save(configuration);

		{
			Check check = new Check();
			check.setName("check example homepage");
			check.setUrl("http://www.example.com/");
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

		System.out.println("*** TEST DATABASE INIT FINISHED ***");
	}

}
