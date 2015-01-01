package net.sf.sitemonitoring.service.check;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

@Slf4j
public abstract class AbstractSingleCheckThread extends AbstractCheckThread {

	protected int connectionTimeoutMillis;
	protected int socketTimeoutMillis;
	private int requiredStatusCode;
	
	protected Map<URI, Object> visitedPages;

	public AbstractSingleCheckThread(int connectionTimeoutMillis, int socketTimeoutMillis, int requiredStatusCode, Map<URI, Object> visitedPages) {
		this.connectionTimeoutMillis = connectionTimeoutMillis;
		this.socketTimeoutMillis = socketTimeoutMillis;
		this.requiredStatusCode = requiredStatusCode;
		this.visitedPages = visitedPages;
	}

	private void addVisitedPage(URI page) {
		visitedPages.put(page, null);
	}

	private boolean isVisitedPage(URI page) {
		return visitedPages.containsKey(page);
	}

	protected boolean checkStatusCode(HttpResponse httpResponse, String url) {
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode != requiredStatusCode) {
			output = "Invalid status: " + url + " required: " + requiredStatusCode + ", received: " + statusCode;
			return false;
		}
		return true;
	}

	protected CloseableHttpResponse doGet(final String url) throws IOException {
		return doRequest(new HttpGet(url));
	}

	protected CloseableHttpResponse doHead(final String url) throws IOException {
		return doRequest(new HttpHead(url));
	}

	private CloseableHttpResponse doRequest(final HttpRequestBase request) throws IOException {
		// optimization
		if (isVisitedPage(request.getURI())) {
			return null;
		} else {
			addVisitedPage(request.getURI());
		}

		if (log.isDebugEnabled()) {
			log.debug(request.getMethod() + " " + request.getURI());
		}
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeoutMillis).setConnectTimeout(connectionTimeoutMillis).build();
		request.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(request);
		} catch (SSLHandshakeException ex) {
			// ignore ValidatorException -> thrown when Java cannot validate
			// certificate
			log.error("java could not validate certificate for URL: " + request.getURI(), ex);
			return null;
		}
		if (log.isDebugEnabled()) {
			log.debug("status: " + response.getStatusLine());
		}
		return response;
	}

}
