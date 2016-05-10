package net.sf.sitemonitoring.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import net.sf.sitemonitoring.entity.Check;

public class CheckServiceTest {
	
	private CheckService checkService;

	@Before
	public void setUp() throws Exception {
		checkService = new CheckService();
	}

	@Test
	public void testIsRangeCheckTrue() throws Exception {
		Check check = new Check();
		check.setName("sitemap [1..100] test");
		check.setUrl("http://www.example.com/sitemap-[1..100].xml");
		assertTrue(checkService.isRangeCheck(check));
	}

	@Test
	public void testIsRangeCheckFalse1() throws Exception {
		Check check = new Check();
		check.setName("sitemap [1..100] test");
		check.setUrl("http://www.example.com/sitemap.xml");
		assertFalse(checkService.isRangeCheck(check));
	}

	@Test
	public void testIsRangeCheckFalse2() throws Exception {
		Check check = new Check();
		check.setName("sitemap [1..100] test");
		check.setUrl("http://www.example.com/sitemap[test].xml");
		assertFalse(checkService.isRangeCheck(check));
	}

	@Test
	public void testIsRangeCheckFalse3() throws Exception {
		Check check = new Check();
		check.setName("sitemap [1..100] test");
		check.setUrl("http://www.example.com/sitemap[a..b].xml");
		assertFalse(checkService.isRangeCheck(check));
	}

	@Test
	public void testGetLowRange() throws Exception {
		Check check = new Check();
		check.setName("sitemap [1..100] test");
		assertEquals(1, checkService.getLowRange(check));
	}

	@Test
	public void testGetHighRange() throws Exception {
		Check check = new Check();
		check.setName("sitemap [1..100] test");
		assertEquals(100, checkService.getHighRange(check));
	}

	@Test
	public void testReplaceRange() throws Exception {
		Check check = new Check();
		check.setName("sitemap [1..100] test");
		check.setUrl("http://www.example.com/sitemap-[1..100].xml");
		checkService.replaceRange(check, 10, 1, 100);
		assertEquals("http://www.example.com/sitemap-10.xml", check.getUrl());
		assertEquals("sitemap 10 test", check.getName());
	}

	@Test
	public void testReplaceRange1() throws Exception {
		Check check = new Check();
		check.setName("test [0..10]");
		check.setUrl("http://www.java-skoleni.cz/test-[0..10].html");
		checkService.replaceRange(check, 3, 0, 10);
		assertEquals("http://www.java-skoleni.cz/test-3.html", check.getUrl());
		assertEquals("test 3", check.getName());
	}

}
