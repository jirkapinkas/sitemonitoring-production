package net.sf.sitemonitoring.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckState;
import net.sf.sitemonitoring.entity.Configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchedulingServiceTest {

	private SchedulingService schedulingService;

	@Mock
	private CheckService checkService;
	
	@Mock
	private ConfigurationService configurationService;

	@Before
	public void setUp() throws Exception {
		schedulingService = new SchedulingService();
		schedulingService.setCheckService(checkService);
		schedulingService.setConfigurationService(configurationService);
	}

	@Test
	public void testDeleteLongRunningChecks() {
		List<Check> checks = new ArrayList<>();
		{
			Check check = new Check();
			check.setCheckState(CheckState.RUNNING);
			Calendar calendar = new GregorianCalendar();
			calendar.add(Calendar.HOUR, -1);
			check.setStartDate(calendar.getTime());
			checks.add(check);
		}
		{
			Check check = new Check();
			check.setCheckState(CheckState.RUNNING);
			Calendar calendar = new GregorianCalendar();
			calendar.add(Calendar.MINUTE, -1);
			check.setStartDate(calendar.getTime());
			checks.add(check);
		}
		{
			Check check = new Check();
			check.setCheckState(CheckState.NOT_RUNNING);
			checks.add(check);
		}
		Configuration configuration = new Configuration();
		configuration.setTooLongRunningCheckMinutes(5);
		Mockito.when(checkService.findAll()).thenReturn(checks);
		Mockito.when(configurationService.find()).thenReturn(configuration);
		schedulingService.deleteLongRunningChecks();
		Mockito.verify(checkService, Mockito.times(1))
					.abort(Mockito.anyInt(), Mockito.anyString());
	}

}
