package net.sf.sitemonitoring.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.CheckResult;
import net.sf.sitemonitoring.push.CheckResultDto;
import net.sf.sitemonitoring.repository.CheckResultRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

@RunWith(MockitoJUnitRunner.class)
public class CheckResultServiceTest {
	
	private CheckResultService checkResultService;
	
	@Mock
	private CheckResultRepository checkResultRepository;

	@Before
	public void setUp() {
		checkResultService = new CheckResultService();
		checkResultService.setCheckResultRepository(checkResultRepository);
	}

	@Test
	public void testGetLastResultsTwoActive() {
		List<Check> checks = new ArrayList<>();
		Check check1 = new Check();
		check1.setActive(true);
		check1.setId(1);
		Check check2 = new Check();
		check2.setActive(true);
		check2.setId(2);
		checks.add(check1);
		checks.add(check2);
		List<CheckResult> checkResults = new ArrayList<>();
		checkResults.add(new CheckResult());
		Mockito.when(checkResultRepository.findByCheck(Mockito.any(Check.class), 
				Mockito.any(PageRequest.class))).thenReturn(checkResults);
		Map<Integer, CheckResultDto> map = checkResultService.getLastResults(checks);
		assertEquals(2, map.size());
		assertNotNull(map.get(1));
		assertNotNull(map.get(2));
	}

	@Test
	public void testGetLastResultsTwoInactive() {
		List<Check> checks = new ArrayList<>();
		Check check1 = new Check();
		check1.setActive(false);
		check1.setId(1);
		Check check2 = new Check();
		check2.setActive(false);
		check2.setId(2);
		checks.add(check1);
		checks.add(check2);
		Map<Integer, CheckResultDto> map = checkResultService.getLastResults(checks);
		assertEquals(0, map.size());
	}

}
