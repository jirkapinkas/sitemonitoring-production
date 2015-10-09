package net.sf.sitemonitoring.service.check;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;

import com.google.common.xml.XmlEscapers;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

@Slf4j
public class JsonCheckThread extends AbstractSingleCheckThread {

	public JsonCheckThread(Check check, Map<URI, Object> visitedPagesGet, Map<URI, Object> visitedPagesHead) {
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
						try {
							Object result = JsonPath.read(webPage, check.getCondition());
							if (result == null) {
								appendMessage("null");
							} else if (!result.toString().equals(check.getTextResult())) {
								appendMessage(check.getUrl() + " has unexpected result: " + XmlEscapers.xmlContentEscaper().escape(result.toString()) + " instead of: "
										+ XmlEscapers.xmlContentEscaper().escape(check.getTextResult()));
							}
						} catch (InvalidPathException ex) {
							appendMessage("Invalid path: " + check.getCondition());
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

		} catch (UnknownHostException ex) {
			try {
				output = check.getUrl() + ": Unknown host: " + new URI(check.getUrl()).getHost();
			} catch (URISyntaxException e) {
				output = check.getUrl() + ": Unknown host: " + check.getUrl();
			}
			log.debug(output, ex);
		} catch (HttpHostConnectException ex) {
			try {
				output = check.getUrl() + ": Cannot connect to: " + new URI(check.getUrl()).getHost();
			} catch (URISyntaxException e) {
				output = check.getUrl() + ": Cannot connect to: " + check.getUrl();
			}
			log.debug(output, ex);
		} catch (IOException ex) {
			output = "Error downloading: " + check.getUrl() + " exception: " + ex.getClass().getName();
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
