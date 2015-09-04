package net.sf.sitemonitoring.service.check;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckCondition;
import net.sf.sitemonitoring.jaxb.sitemap.Urlset;
import net.sf.sitemonitoring.jaxb.sitemapindex.Sitemapindex;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.eventbus.EventBus;

@RunWith(MockitoJUnitRunner.class)
public class SitemapCheckServiceTest {

	private SitemapCheckThread sitemapCheckThread;

	@Mock
	private SinglePageCheckService singlePageCheckServiceMock;

	private Map<URI, Object> visitedPagesGet;
	private Map<URI, Object> visitedPagesHead;

	private static final int timeout = 1000;

	@Before
	public void before() throws JAXBException {
		visitedPagesGet = new HashMap<URI, Object>();
		visitedPagesHead = new HashMap<URI, Object>();
		SinglePageCheckService singlePageCheckService = new SinglePageCheckService();
		singlePageCheckService.setEventBus(new EventBus());
		sitemapCheckThread = new SitemapCheckThread(singlePageCheckService, null);
	}

	@Test
	public void testConvertSitemap() throws Exception {
		String sitemapXml = FileUtils.readFileToString(new File("src/test/resources/sitemap.xml"));
		Urlset urlset = sitemapCheckThread.readSitemap(sitemapXml);
		assertEquals(2, urlset.getUrls().size());
		assertEquals("http://www.sqlvids.com/", urlset.getUrls().get(0).getLoc());
	}

	@Test
	public void testConvertSitemapIndex() throws Exception {
		String sitemapIndexXml = FileUtils.readFileToString(new File("src/test/resources/sitemap-index.xml"));
		Sitemapindex sitemapindex = sitemapCheckThread.readSitemapIndex(sitemapIndexXml);
		assertEquals(2, sitemapindex.getSitemaps().size());
		assertEquals("http://localhost:8081/local-sitemap.xml", sitemapindex.getSitemaps().get(0).getLoc());
	}

	@Test(expected = JAXBException.class)
	public void testConvertSitemapError() throws Exception {
		String sitemapXml = FileUtils.readFileToString(new File("src/test/resources/sitemap.corrupt.xml"));
		Urlset urlset = sitemapCheckThread.readSitemap(sitemapXml);
		assertEquals(2, urlset.getUrls().size());
		assertEquals("http://www.sqlvids.com/", urlset.getUrls().get(0).getLoc());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCheckSitemapErrors() throws Exception {
		Mockito.when(singlePageCheckServiceMock.performCheck(Mockito.any(Check.class), Mockito.any(Map.class), Mockito.any(Map.class))).thenReturn("Error!");
		sitemapCheckThread.setSinglePageCheckService(singlePageCheckServiceMock);

		String sitemapXml = FileUtils.readFileToString(new File("src/test/resources/sitemap.xml"));
		Urlset urlset = sitemapCheckThread.readSitemap(sitemapXml);
		Check sitemapCheck = new Check();
		sitemapCheck.setConditionType(CheckCondition.CONTAINS);
		sitemapCheck.setCondition("</html>");
		sitemapCheck.setCheckBrokenLinks(false);
		sitemapCheck.setSocketTimeout(timeout);
		sitemapCheck.setConnectionTimeout(timeout);
		String checkResult = sitemapCheckThread.check(urlset, sitemapCheck, null, null);
		assertEquals("Error!<br />Error!<br />", checkResult);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCheckSitemapNoErrors() throws Exception {
		Mockito.when(singlePageCheckServiceMock.performCheck(Mockito.any(Check.class), Mockito.any(Map.class), Mockito.any(Map.class))).thenReturn(null);
		sitemapCheckThread.setSinglePageCheckService(singlePageCheckServiceMock);

		String sitemapXml = FileUtils.readFileToString(new File("src/test/resources/sitemap.xml"));
		Urlset urlset = sitemapCheckThread.readSitemap(sitemapXml);
		Check sitemapCheck = new Check();
		sitemapCheck.setConditionType(CheckCondition.CONTAINS);
		sitemapCheck.setCondition("</html>");
		sitemapCheck.setCheckBrokenLinks(false);
		sitemapCheck.setSocketTimeout(timeout);
		sitemapCheck.setConnectionTimeout(timeout);
		String checkResult = sitemapCheckThread.check(urlset, sitemapCheck, visitedPagesGet, visitedPagesHead);
		assertNull(checkResult);
	}

}
