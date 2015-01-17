package net.sf.sitemonitoring.service.check;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.jaxb.sitemap.Url;
import net.sf.sitemonitoring.jaxb.sitemap.Urlset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

@Slf4j
public class SitemapCheckThread extends AbstractCheckThread {

	private JAXBContext jaxbContext;
	private SinglePageCheckService singlePageCheckService;

	public SitemapCheckThread(JAXBContext jaxbContext, SinglePageCheckService singlePageCheckService, Check check) {
		super(check);
		this.jaxbContext = jaxbContext;
		this.singlePageCheckService = singlePageCheckService;
	}

	protected String downloadSitemap(CloseableHttpClient httpClient, String url) throws IOException {
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(new HttpGet(url));
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				throw new IOException();
			}
			HttpEntity entity = httpResponse.getEntity();
			return EntityUtils.toString(entity);
		} catch (IOException ex) {
			throw new IOException("Error downloading sitemap: " + url, ex);
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					log.error("Error closing response", e);
				}
			}
		}
	}

	protected Urlset readSitemap(String sitemapXml) throws JAXBException {
		InputStream inputStream = null;
		try {
			inputStream = IOUtils.toInputStream(sitemapXml, "UTF-8");
			return (Urlset) jaxbContext.createUnmarshaller().unmarshal(inputStream);
		} catch (JAXBException e) {
			throw new JAXBException("Error reading sitemap", e);
		} catch (IOException e) {
			throw new JAXBException("Cannot convert sitemap using UTF-8", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("Error closing input stream", e);
				}
			}
		}
	}

	protected String check(Urlset urlset, Check sitemapCheck, Map<URI, Object> visitedPagesGet, Map<URI, Object> visitedPagesHead) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Url url : urlset.getUrls()) {
			if (abort) {
				return "aborted";
			}
			if (SinglePageCheckService.ignoreUrl(url.getLoc(), sitemapCheck.getExcludedUrls())) {
				log.debug("ignore url: " + url.getLoc());
				continue;
			}
			Check singleCheck = new Check();
			copyConnectionSettings(sitemapCheck, singleCheck);
			singleCheck.setId(sitemapCheck.getId());
			singleCheck.setCondition(sitemapCheck.getCondition());
			singleCheck.setConditionType(sitemapCheck.getConditionType());
			singleCheck.setReturnHttpCode(sitemapCheck.getReturnHttpCode());
			singleCheck.setUrl(url.getLoc());
			singleCheck.setDoNotFollowUrls(sitemapCheck.getDoNotFollowUrls());
			singleCheck.setCheckBrokenLinks(sitemapCheck.isCheckBrokenLinks());
			String checkResultTxt = singlePageCheckService.performCheck(singleCheck, visitedPagesGet, visitedPagesHead);
			if (checkResultTxt != null) {
				stringBuilder.append(checkResultTxt);
				stringBuilder.append("<br />");
			}
		}
		if (stringBuilder.toString().isEmpty()) {
			return null;
		}
		return stringBuilder.toString();
	}

	@Override
	public void performCheck() {
		Map<URI, Object> visitedPagesGet = new HashMap<URI, Object>();
		Map<URI, Object> visitedPagesHead = new HashMap<URI, Object>();
		log.debug("sitemap performCheck() start");
		try {
			String sitemapXml = downloadSitemap(httpClient, check.getUrl());
			Urlset urlset = readSitemap(sitemapXml);
			log.debug("sitemap contains " + urlset.getUrls().size() + " urls");
			output = check(urlset, check, visitedPagesGet, visitedPagesHead);
		} catch (JAXBException e) {
			log.error("JAXB exception", e);
			output = "Invalid sitemap: " + check.getUrl();
		} catch (IOException e) {
			log.error("Error executing sitemap", e);
			output = e.getMessage();
		} 
		log.debug("sitemap performCheck() finish");
	}

	public void setSinglePageCheckService(SinglePageCheckService singlePageCheckService) {
		this.singlePageCheckService = singlePageCheckService;
	}

}
