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

	@Test
	public void testIgnoreUrlShouldIgnoreUrl() {
		String url = "http://www.seznam.cz/*";
		String excludedUrls = "http://www.seznam.cz*\r\nhttp://www.google.com*";
		Assert.assertTrue(SinglePageCheckService.ignoreUrl(url, excludedUrls));
	}

	@Test
	public void testIgnoreUrlShouldNotIgnoreUrl() {
		String url = "http://www.seznamka.cz/*";
		String excludedUrls = "http://www.seznam.cz*\r\nhttp://www.google.com*";
		Assert.assertFalse(SinglePageCheckService.ignoreUrl(url, excludedUrls));
	}

}
