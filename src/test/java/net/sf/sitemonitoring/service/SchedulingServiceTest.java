package net.sf.sitemonitoring.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckState;
import net.sf.sitemonitoring.entity.Configuration;

@RunWith(MockitoJUnitRunner.class)
public class SchedulingServiceTest {

	private SchedulingService schedulingService;

	@Mock
	private CheckService checkService;
	
	@Mock
	private ConfigurationService configurationService;

	@Mock
	private CheckResultService checkResultService;

	@Before
	public void setUp() throws Exception {
		schedulingService = new SchedulingService();
		schedulingService.setCheckService(checkService);
		schedulingService.setConfigurationService(configurationService);
		schedulingService.setCheckResultService(checkResultService);
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

	@Test
	public void testDeleteHistoricData() {
		List<Check> checks = new ArrayList<>();
		{
			Check check = new Check();
			check.setCheckState(CheckState.RUNNING);
			Calendar calendar = new GregorianCalendar();
			calendar.add(Calendar.HOUR, -1);
			check.setStartDate(calendar.getTime());
			check.setId(1);
			checks.add(check);
		}
		{
			Check check = new Check();
			check.setCheckState(CheckState.RUNNING);
			Calendar calendar = new GregorianCalendar();
			calendar.add(Calendar.MINUTE, -1);
			check.setStartDate(calendar.getTime());
			check.setId(2);
			checks.add(check);
		}
		{
			Check check = new Check();
			check.setCheckState(CheckState.NOT_RUNNING);
			check.setId(3);
			checks.add(check);
		}
		Mockito.when(checkService.findAll()).thenReturn(checks);
		schedulingService.deleteHistoricData();
		Mockito.verify(checkResultService, Mockito.times(3)).deleteOld(Mockito.anyInt(), Mockito.any(Date.class));
		Mockito.verify(checkResultService, Mockito.times(1)).deleteOld(Mockito.eq(1), Mockito.any(Date.class));
		Mockito.verify(checkResultService, Mockito.times(1)).deleteOld(Mockito.eq(2), Mockito.any(Date.class));
		Mockito.verify(checkResultService, Mockito.times(1)).deleteOld(Mockito.eq(3), Mockito.any(Date.class));
		Mockito.verify(checkResultService, Mockito.times(0)).deleteOld(Mockito.eq(4), Mockito.any(Date.class));
	}

}
