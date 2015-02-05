package net.sf.sitemonitoring.service.check;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckCondition;
import net.sf.sitemonitoring.entity.Check.CheckType;
import net.sf.sitemonitoring.entity.Check.HttpMethod;
import net.sf.sitemonitoring.entity.Credentials;
import net.sf.sitemonitoring.jaxb.sitemap.Url;
import net.sf.sitemonitoring.jaxb.sitemap.Urlset;
import net.sf.sitemonitoring.service.check.util.JettyServerUtil;
import net.sf.sitemonitoring.service.check.util.ProxyServerUtil;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;

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

	private SpiderCheckThread spiderCheckThread;

	private static Server jettyServer;
	
	private static HttpProxyServer httpProxyServer;

	public static final String TEST_JETTY_HTTP = "http://localhost:8081/";

	private static final int timeout = 2000;

	@BeforeClass
	public static void setUp() throws Exception {
		jettyServer = JettyServerUtil.start();
		httpProxyServer = ProxyServerUtil.start();
	}
	
	public static void main(String[] args) throws Exception {
		JettyServerUtil.start();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		JettyServerUtil.stop(jettyServer); // this will also stop proxy
		ProxyServerUtil.stop(httpProxyServer);
	}

	@Before
	public void before() throws JAXBException {
		singlePageCheckService = new SinglePageCheckService();
		singlePageCheckService.setEventBus(new EventBus());
		sitemapCheckThread = new SitemapCheckThread(JAXBContext.newInstance(Urlset.class, Url.class), singlePageCheckService, null);
		// created in AbstractCheckThread.run(), that's why I have to create it
		// here.
		sitemapCheckThread.httpClient = HttpClients.createDefault();
		spiderCheckThread = new SpiderCheckThread(singlePageCheckService, null);
		spiderCheckThread.httpClient = HttpClients.createDefault();
	}

	@After
	public void after() throws IOException {
		sitemapCheckThread.httpClient.close();
		spiderCheckThread.httpClient.close();
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
		check.setHttpMethod(HttpMethod.GET);

		assertNull(singlePageCheckService.performCheck(check));
	}

	@Test
	public void testPerformCheckSinglePageContainsWithProxy() throws Exception {
		Check check = new Check();
		check.setCondition("</html>");
		check.setReturnHttpCode(200);
		check.setType(CheckType.SINGLE_PAGE);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setUrl(TEST_JETTY_HTTP + "index.html");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);
		check.setHttpProxyServer("localhost");
		check.setHttpProxyPort(8082);
		check.setHttpProxyUsername("test");
		check.setHttpProxyPassword("works");
		check.setHttpMethod(HttpMethod.GET);

		assertNull(singlePageCheckService.performCheck(check));
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
		check.setHttpMethod(HttpMethod.GET);

		assertNull(singlePageCheckService.performCheck(check));
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
		check.setHttpMethod(HttpMethod.GET);

		sitemapCheckThread.check = check;
		sitemapCheckThread.performCheck();
		assertNull(sitemapCheckThread.output);
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
		check.setHttpMethod(HttpMethod.GET);

		assertNull(singlePageCheckService.performCheck(check));
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
		check.setHttpMethod(HttpMethod.GET);

		assertEquals("Invalid status: http://localhost:8081/not-exists.html required: 200, received: 404", singlePageCheckService.performCheck(check));
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
		check.setHttpMethod(HttpMethod.HEAD);

		assertNull(singlePageCheckService.performCheck(check));
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
		check.setHttpMethod(HttpMethod.HEAD);

		assertEquals("Incorrect URL: http://", singlePageCheckService.performCheck(check));
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
		check.setHttpMethod(HttpMethod.GET);

		assertNull(singlePageCheckService.performCheck(check));
	}

	@Test
	public void testDownloadSitemap() throws Exception {
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			String sitemapXml = sitemapCheckThread.downloadSitemap(httpClient, TEST_JETTY_HTTP + "sitemap.xml");
			assertTrue(sitemapXml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
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
		assertNull(sitemapCheckThread.output);
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

	@Test
	public void testSpiderWithoutBrokenLinks() {
		Check check = new Check();
		check.setReturnHttpCode(200);
		check.setType(CheckType.SPIDER);
		// note: URL must be base URL (directory!), not some web page
		check.setUrl(TEST_JETTY_HTTP + "spider/");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);
		spiderCheckThread.check = check;
		spiderCheckThread.performCheck();
		assertEquals(
				"http://localhost:8081/spider/ has error: Invalid status: http://localhost:8081/spider/broken-link.html required: 200, received: 404<br />http://localhost:8081/spider/contains-broken-links.html has error: Invalid status: http://localhost:8081/spider/doesnt-exist required: 200, received: 404<br />http://localhost:8081/spider/page?id=9 has error: Invalid status: http://localhost:8081/spider/not-found.html required: 200, received: 404<br />",
				spiderCheckThread.output);

	}

	// TODO Somehow this doesn't show the same error on Ubuntu. I must investigate more why the test differs.
	@Ignore
	@Test
	public void testSpiderWithBrokenLinks() {
		Check check = new Check();
		check.setReturnHttpCode(200);
		check.setType(CheckType.SPIDER);
		check.setUrl(TEST_JETTY_HTTP + "spider/");
		check.setCheckBrokenLinks(true);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);
		spiderCheckThread.check = check;
		spiderCheckThread.performCheck();
		// TODO prints more than necessary
		assertEquals(
				"http://localhost:8081/spider/ has error: Invalid status: http://localhost:8081/spider/broken-link.html required: 200, received: 404<br />http://localhost:8081/spider/ has error: http://localhost:8081/spider/contains-broken-links.html has error: Invalid status: http://localhost:8081/spider/doesnt-exist required: 200, received: 404<br />http://localhost:8081/spider/contains-broken-links.html has error: Error downloading: http://www.doesntexist93283893289292947987498.com/<br /><br />http://localhost:8081/spider/ has error: Invalid status: http://localhost:8081/spider/broken-link.html required: 200, received: 404<br />http://localhost:8081/spider/contains-broken-links.html has error: Invalid status: http://localhost:8081/spider/doesnt-exist required: 200, received: 404<br />http://localhost:8081/spider/page?id=8 has error: http://localhost:8081/spider/page?id=9 has error: Invalid status: http://localhost:8081/spider/not-found.html required: 200, received: 404<br /><br />http://localhost:8081/spider/page?id=9 has error: Invalid status: http://localhost:8081/spider/not-found.html required: 200, received: 404<br />",
				spiderCheckThread.output);
	}

	@Test
	public void testPerformCheckSitemapWithErrorsNoBrokenLinks() throws Exception {
		Check check = new Check();
		check.setType(CheckType.SITEMAP);
		check.setReturnHttpCode(200);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setCondition("</html>");
		check.setExcludedUrls("*pdf");
		check.setUrl(TEST_JETTY_HTTP + "local-sitemap-with-errors.xml");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);
		check.setHttpMethod(HttpMethod.GET);

		sitemapCheckThread.check = check;
		sitemapCheckThread.performCheck();
		assertEquals("Invalid status: http://localhost:8081/doesnt-exist required: 200, received: 404<br />", sitemapCheckThread.output);
	}

	@Test
	public void testPerformCheckSitemapWithErrorsAndBrokenLinks() throws Exception {
		Check check = new Check();
		check.setType(CheckType.SITEMAP);
		check.setReturnHttpCode(200);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setCondition("</html>");
		check.setExcludedUrls("*pdf");
		check.setUrl(TEST_JETTY_HTTP + "local-sitemap-with-errors.xml");
		check.setCheckBrokenLinks(true);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);
		check.setHttpMethod(HttpMethod.GET);

		sitemapCheckThread.check = check;
		sitemapCheckThread.performCheck();
		assertEquals(
				"Invalid status: http://localhost:8081/doesnt-exist required: 200, received: 404<br />http://localhost:8081/contains-broken-links.html has error: Invalid status: http://localhost:8081/doesnt-exist required: 200, received: 404<br />http://localhost:8081/contains-broken-links.html has error: Error downloading: http://www.doesntexist93283893289292947987498.com/<br /><br />",
				sitemapCheckThread.output);
	}

	@Test
	public void testSingleCheckBasicAuthentication() {
		Check check = new Check();
		check.setCondition("this is protected using BasicAuthenticationFilter.java");
		check.setReturnHttpCode(200);
		check.setType(CheckType.SINGLE_PAGE);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setUrl(TEST_JETTY_HTTP + "security/basic.html");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);
		check.setHttpMethod(HttpMethod.GET);
		
		Credentials credentials = new Credentials();
		credentials.setUsername("admin");
		credentials.setPassword("admin");
		check.setCredentials(credentials);

		assertNull(singlePageCheckService.performCheck(check));
	}


	@Test
	public void testSingleCheckBasicAuthenticationWithProxy() throws Exception {
		Check check = new Check();
		check.setCondition("this is protected using BasicAuthenticationFilter.java");
		check.setReturnHttpCode(200);
		check.setType(CheckType.SINGLE_PAGE);
		check.setConditionType(CheckCondition.CONTAINS);
		check.setUrl(TEST_JETTY_HTTP + "security/basic.html");
		check.setCheckBrokenLinks(false);
		check.setSocketTimeout(timeout);
		check.setConnectionTimeout(timeout);
		check.setHttpProxyServer("localhost");
		check.setHttpProxyPort(8082);
		check.setHttpProxyUsername("test");
		check.setHttpProxyPassword("works");
		check.setHttpMethod(HttpMethod.GET);

		Credentials credentials = new Credentials();
		credentials.setUsername("admin");
		credentials.setPassword("admin");
		check.setCredentials(credentials);

		assertNull(singlePageCheckService.performCheck(check));
	}


}
