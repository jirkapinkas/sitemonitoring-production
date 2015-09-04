package net.sf.sitemonitoring.service.check;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckType;
import net.sf.sitemonitoring.entity.Check.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.xml.XmlEscapers;

@Slf4j
public class SinglePageCheckThread extends AbstractSingleCheckThread {

	public SinglePageCheckThread(Check check, Map<URI, Object> visitedPagesGet, Map<URI, Object> visitedPagesHead) {
		super(check, visitedPagesGet, visitedPagesHead);
	}

	@Override
	public void performCheck() {
		log.debug("start perform check");
		CloseableHttpResponse httpResponse = null;
		try {
			if (check.getHttpMethod() == HttpMethod.HEAD) {
				httpResponse = doHead(check.getUrl());
				if (httpResponse == null) {
					return;
				} else {
					checkStatusCode(httpResponse, check.getUrl());
				}
			} else if (check.getHttpMethod() == HttpMethod.GET) {
				httpResponse = doGet(check.getUrl());
				if (httpResponse == null) {
					return;
				}
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					String webPage = EntityUtils.toString(entity);
					if (check.isStoreWebpage()) {
						check.setWebPage(webPage);
					}
					if (checkStatusCode(httpResponse, check.getUrl()) && check.getCondition() != null && !check.getCondition().isEmpty()) {
						switch (check.getConditionType()) {
						case CONTAINS:
							if (!webPage.contains(check.getCondition())) {
								appendMessage(check.getUrl() + " doesn't contain " + XmlEscapers.xmlContentEscaper().escape(check.getCondition()));
							}
							break;
						case DOESNT_CONTAIN:
							if (webPage.contains(check.getCondition())) {
								appendMessage(check.getUrl() + " contains " + XmlEscapers.xmlContentEscaper().escape(check.getCondition()));
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
								boolean skip = false;
								if (check.getFollowOutboundBrokenLinks() == null || check.getFollowOutboundBrokenLinks() == false) {
									if (!SinglePageCheckService.isSameDomain(url, check.getUrl())) {
										skip = true;
									}
								}
								if (!skip) {
									Check subCheck = new Check();
									copyConnectionSettings(check, subCheck);
									subCheck.setId(check.getId());
									subCheck.setUrl(url);
									subCheck.setType(CheckType.SINGLE_PAGE);
									subCheck.setCheckBrokenLinks(check.isCheckBrokenLinks());
									subCheck.setHttpMethod(HttpMethod.HEAD);
									SinglePageCheckThread checkThread = new SinglePageCheckThread(subCheck, visitedPagesGet, visitedPagesHead);
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
			} else {
				throw new UnsupportedOperationException("Unknown HTTP METHOD: " + check.getHttpMethod());
			}
			log.debug("check successful");
		} catch (IllegalArgumentException ex) {
			output = check.getUrl() + " has error: incorrect URL: " + check.getUrl();
			log.debug(output, ex);
		} catch (ConnectTimeoutException ex) {
			output = check.getUrl() + " has error: connect timeout: " + check.getUrl();
			log.debug(output, ex);
		} catch (SocketTimeoutException ex) {
			output = check.getUrl() + " has error: socket timeout: " + check.getUrl();
			log.debug(output, ex);
		} catch (IOException ex) {
			output = check.getUrl() + " has error: error downloading: " + check.getUrl();
			log.debug(output, ex);
		} catch (Exception ex) {
			output = check.getUrl() + " has error: " + ex.getMessage();
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
