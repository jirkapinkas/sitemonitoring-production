package net.sf.sitemonitoring.service.check;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckCondition;
import net.sf.sitemonitoring.entity.Check.CheckType;
import net.sf.sitemonitoring.jaxb.sitemap.Url;
import net.sf.sitemonitoring.jaxb.sitemap.Urlset;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

/**
 * This class tests all checks, which perform http requests
 * 
 * @author pinkas
 *
 */
public class HttpCheckServiceTest {

	private SinglePageCheckService singlePageCheckService;

	private SitemapCheckThread sitemapCheckThread;

	private static Server server;

	public static final String TEST_JETTY_HTTP = "http://localhost:8081/";

	private static final int timeout = 1000;

	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println("*** STARTED TEST JETTY SERVER ***");
		server = new Server(8081);

		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/");
		webAppContext.setWar("src/test/resources/test-app.war");
		server.setHandler(webAppContext);

		server.start();
	}

	@Before
	public void before() throws JAXBException {
		singlePageCheckService = new SinglePageCheckService();
		singlePageCheckService.setEventBus(new EventBus());
		sitemapCheckThread = new SitemapCheckThread(JAXBContext.newInstance(Urlset.class, Url.class), singlePageCheckService, null);
		// created in AbstractCheckThread.run(), that's why I have to create it here.
		sitemapCheckThread.httpClient = HttpClients.createDefault();
	}
	
	@After
	public void after() throws IOException {
		sitemapCheckThread.httpClient.close();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		server.stop();
		System.out.println("*** STOPPED TEST JETTY SERVER ***");
	}

	@Test
	public void testPerformCheckSinglePageContains() throws Exception {
		Check check = new Check();
		check.setCondition("</html>");
		check.setReturnHttpCode(200);
		check.setType(CheckType.SINGLE_PAGE);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setUrl(TEST_JETTY_HTTP + "index.html");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);

		Assert.assertNull(singlePageCheckService.performCheck(check));
	}

	@Test
	public void testPerformCheckSinglePageDoNotFollow() throws Exception {
		Check check = new Check();
		check.setType(CheckType.SINGLE_PAGE);
		check.setReturnHttpCode(200);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setCondition("</html>");
		check.setDoNotFollowUrls("*do-not-follow*\r\n*twitter.com");
		check.setUrl(TEST_JETTY_HTTP + "test-do-not-follow.html");
		check.setCheckBrokenLinks(true);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);

		Assert.assertNull(singlePageCheckService.performCheck(check));
	}

	@Test
	public void testPerformCheckSitemapPageDoNotFollow() throws Exception {
		Check check = new Check();
		check.setType(CheckType.SITEMAP);
		check.setReturnHttpCode(200);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setCondition("</html>");
		check.setDoNotFollowUrls("*do-not-follow*\r\n*twitter.com");
		check.setExcludedUrls("*pdf");
		check.setUrl(TEST_JETTY_HTTP + "local-sitemap.xml");
		check.setCheckBrokenLinks(true);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);

		sitemapCheckThread.check = check;
		sitemapCheckThread.performCheck();
		Assert.assertNull(sitemapCheckThread.output);
	}

	@Test
	public void testPerformCheckSinglePageDoesntContain() throws Exception {
		Check check = new Check();
		check.setCondition("not there");
		check.setReturnHttpCode(200);
		check.setType(CheckType.SINGLE_PAGE);
		check.setConditionType(CheckCondition.DOESNT_CONTAIN);
		check.setUrl(TEST_JETTY_HTTP + "index.html");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);

		Assert.assertNull(singlePageCheckService.performCheck(check));
	}

	@Test
	public void testPerformCheckSinglePageUnexpectedDoesntExist() throws Exception {
		Check check = new Check();
		check.setReturnHttpCode(200);
		check.setType(CheckType.SINGLE_PAGE);
		check.setUrl(TEST_JETTY_HTTP + "not-exists.html");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);

		Assert.assertEquals("Invalid status: http://localhost:8081/not-exists.html required: 200, received: 404", singlePageCheckService.performCheck(check));
	}

	@Test
	public void testPerformCheckSinglePageExpectedDoesntExist() throws Exception {
		Check check = new Check();
		check.setReturnHttpCode(404);
		check.setType(CheckType.SINGLE_PAGE);
		check.setUrl(TEST_JETTY_HTTP + "not-exists.html");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);

		Assert.assertNull(singlePageCheckService.performCheck(check));
	}

	@Test
	public void testPerformCheckBadUrl() throws Exception {
		Check check = new Check();
		check.setReturnHttpCode(404);
		check.setType(CheckType.SINGLE_PAGE);
		check.setUrl("http://");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);

		Assert.assertEquals("Incorrect URL: http://", singlePageCheckService.performCheck(check));
	}

	@Test
	public void testPerformCheckPdfExists() throws Exception {
		Check check = new Check();
		check.setReturnHttpCode(200);
		check.setType(CheckType.SINGLE_PAGE);
		check.setUrl(TEST_JETTY_HTTP + "test.pdf");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);

		Assert.assertNull(singlePageCheckService.performCheck(check));
	}

	@Test
	public void testDownloadSitemap() throws Exception {
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			String sitemapXml = sitemapCheckThread.downloadSitemap(httpClient, TEST_JETTY_HTTP + "sitemap.xml");
			Assert.assertTrue(sitemapXml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
		}
	}

	@Test
	public void testCheckSitemap() throws Exception {
		Check check = new Check();
		check.setCondition("</html>");
		check.setReturnHttpCode(200);
		check.setType(CheckType.SITEMAP);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setUrl(TEST_JETTY_HTTP + "sitemap.xml");
		check.setExcludedUrls("*html\r\nhttp://www.sqlvids.com/\r\n*pdf");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);
		
		sitemapCheckThread.check = check;
		sitemapCheckThread.performCheck();
		Assert.assertNull(sitemapCheckThread.output);
	}

	@Test(expected = IOException.class)
	public void testDownloadSitemapError() throws Exception {
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			sitemapCheckThread.downloadSitemap(httpClient, TEST_JETTY_HTTP + "sitemap.notexists.xml");
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
		}
	}

}
