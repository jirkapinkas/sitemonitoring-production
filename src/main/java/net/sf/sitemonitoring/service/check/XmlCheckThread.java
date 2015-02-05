package net.sf.sitemonitoring.service.check;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.HttpMethod;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.util.EntityUtils;

import com.google.common.xml.XmlEscapers;

@Slf4j
public class XmlCheckThread extends AbstractSingleCheckThread {

	public XmlCheckThread(Check check, Map<URI, Object> visitedPagesGet, Map<URI, Object> visitedPagesHead) {
		super(check, visitedPagesGet, visitedPagesHead);
	}

	@Override
	public void performCheck() {
		log.debug("start perform check");
		CloseableHttpResponse httpResponse = null;
		try {
			if(check.getHttpMethod() == HttpMethod.HEAD) {
				httpResponse = doHead(check.getUrl());
				if (httpResponse == null) {
					return;
				} else {
					checkStatusCode(httpResponse, check.getUrl());
				}
			} else if(check.getHttpMethod() == HttpMethod.GET) {
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
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						org.w3c.dom.Document doc = builder.parse(IOUtils.toInputStream(webPage, "UTF-8"));
						XPathFactory xPathfactory = XPathFactory.newInstance();
						XPath xpath = xPathfactory.newXPath();
						XPathExpression expr = xpath.compile(check.getCondition());
						// TODO Retrieve whole XML fragment
						String result = expr.evaluate(doc);
						if(!result.equals(check.getTextResult())) {
							appendMessage(check.getUrl() + " has unexpected result: " + XmlEscapers.xmlContentEscaper().escape(result) + " instead of: " + XmlEscapers.xmlContentEscaper().escape(check.getTextResult()));
						}
					}
				}
			} else {
				throw new UnsupportedOperationException("Unknown HTTP METHOD: " + check.getHttpMethod());
			}
			log.debug("check successful");
		} catch (IllegalArgumentException ex) {
			output = "Incorrect URL: " + check.getUrl();
			log.debug(output, ex);
		} catch (ConnectTimeoutException ex) {
			output = "Connect timeout: " + check.getUrl();
			log.debug(output, ex);
		} catch (SocketTimeoutException ex) {
			output = "Socket timeout: " + check.getUrl();
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
