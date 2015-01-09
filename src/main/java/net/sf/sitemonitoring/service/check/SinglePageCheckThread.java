package net.sf.sitemonitoring.service.check;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckType;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
public class SinglePageCheckThread extends AbstractSingleCheckThread {

	public SinglePageCheckThread(Check check, Map<URI, Object> visitedPages) {
		super(check.getConnectionTimeout(), check.getSocketTimeout(), check.getReturnHttpCode(), visitedPages);
		this.check = check;
		this.visitedPages = visitedPages;
	}

	@Override
	public void performCheck() {
		log.debug("start perform check");
		CloseableHttpResponse httpResponse = null;
		try {
			if ((check.getCondition() == null || check.getCondition().isEmpty()) && !check.isStoreWebpage()) {
				httpResponse = doHead(check.getUrl());
				if (httpResponse == null) {
					return;
				} else {
					checkStatusCode(httpResponse, check.getUrl());
				}
			} else {
				httpResponse = doGet(check.getUrl());
				if (httpResponse == null) {
					return;
				}
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					String webPage = EntityUtils.toString(entity);
					if(check.isStoreWebpage()) {
						check.setWebPage(webPage);
					}
					if (checkStatusCode(httpResponse, check.getUrl())) {
						switch (check.getConditionType()) {
						case CONTAINS:
							if (!webPage.contains(check.getCondition())) {
								appendMessage(check.getUrl() + " doesn't contain " + check.getCondition());
							}
							break;
						case DOESNT_CONTAIN:
							if (webPage.contains(check.getCondition())) {
								appendMessage(check.getUrl() + " contains " + check.getCondition());
							}
							break;
						}
					}

					if (check.isCheckBrokenLinks()) {
						Document document = Jsoup.parse(webPage);
						Elements newsHeadlines = document.select("a");
						Iterator<Element> iterator = newsHeadlines.iterator();
						while (iterator.hasNext()) {
							if (abort) {
								appendMessage("aborted");
								break;
							}
							Element element = (Element) iterator.next();
							element.setBaseUri(check.getUrl());
							String url = element.absUrl("href").trim();

							if (!url.isEmpty() && !url.startsWith("mailto:") && !SinglePageCheckService.ignoreUrl(url, check.getDoNotFollowUrls())) {
								Check subCheck = new Check();
								subCheck.setId(check.getId());
								subCheck.setUrl(url);
								subCheck.setType(CheckType.SINGLE_PAGE);
								subCheck.setConnectionTimeout(check.getConnectionTimeout());
								subCheck.setSocketTimeout(check.getSocketTimeout());
								subCheck.setCheckBrokenLinks(check.isCheckBrokenLinks());
								SinglePageCheckThread checkThread = new SinglePageCheckThread(subCheck, visitedPages);
								log.debug("check sub-link: " + subCheck.getUrl());
								checkThread.start();
								checkThread.join();
								if (checkThread.getOutput() != null && !checkThread.getOutput().trim().isEmpty()) {
									appendMessage(check.getUrl() + " has error: " + checkThread.getOutput() + "<br />");
								}
							}
						}
					}
				}
			}
			log.debug("check successful");
		} catch (IllegalArgumentException ex) {
			output = "Incorrect URL: " + check.getUrl();
			log.debug(output, ex);
		} catch (IOException ex) {
			output = "Error downloading: " + check.getUrl();
			log.debug(output, ex);
		} catch (Exception ex) {
			output = "Error: " + ex.getMessage();
			log.debug(output, ex);
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

}
