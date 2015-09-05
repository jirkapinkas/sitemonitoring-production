package net.sf.sitemonitoring.service.check;

import org.junit.Assert;
import org.junit.Test;

public class SinglePageCheckServiceTest {

	@Test
	public void testIsSameDomain() {
		Assert.assertTrue(SinglePageCheckService.isSameDomain("http://www.javalibs.com/a/b", "http://www.javalibs.com/b/a"));
		Assert.assertTrue(SinglePageCheckService.isSameDomain("http://www.javalibs.com/a/b", "http://javalibs.com/b/a"));
		Assert.assertTrue(SinglePageCheckService.isSameDomain("https://www.javalibs.com/a/b", "http://javalibs.com/b/a"));
		Assert.assertFalse(SinglePageCheckService.isSameDomain("https://test.javalibs.com/a/b", "http://javalibs.com/b/a"));
		Assert.assertFalse(SinglePageCheckService.isSameDomain("https:///test.javalibs.com/a/b", "http://javalibs.com/b/a"));
	}

}
