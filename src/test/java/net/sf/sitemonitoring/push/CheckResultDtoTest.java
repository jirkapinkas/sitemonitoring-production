package net.sf.sitemonitoring.push;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.Test;

public class CheckResultDtoTest {

	CheckResultDto checkResultDto;

	@Before
	public void setUp() throws Exception {
		checkResultDto = new CheckResultDto();
	}

	@Test
	public void testGetDateInterval1A() throws ParseException {
		checkResultDto.setStartDate(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("01.01.2015 03:00:00"));
		checkResultDto.setFinishDate(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("01.01.2015 03:00:10"));
		assertEquals("01.01.2015 03:00:00 - 03:00:10", checkResultDto.getDateInterval());
	}

	@Test
	public void testGetDateInterval1B() throws ParseException {
		checkResultDto.setStartDate(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("01.01.2015 03:00:00"));
		checkResultDto.setFinishDate(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("01.02.2015 03:00:00"));
		assertEquals("01.01.2015 03:00:00 - 01.02.2015 03:00:00", checkResultDto.getDateInterval());
	}

	@Test
	public void testGetDateInterval2() throws ParseException {
		checkResultDto.setStartDate(null);
		checkResultDto.setFinishDate(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("01.01.2015 03:00:10"));
		assertEquals("", checkResultDto.getDateInterval());
	}

	@Test
	public void testGetDateInterval3() throws ParseException {
		checkResultDto.setStartDate(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("01.01.2015 03:00:00"));
		checkResultDto.setFinishDate(null);
		assertEquals("01.01.2015 03:00:00 - ", checkResultDto.getDateInterval());
	}

	@Test
	public void testGetDateInterval4() throws ParseException {
		assertEquals("", checkResultDto.getDateInterval());
	}

}
