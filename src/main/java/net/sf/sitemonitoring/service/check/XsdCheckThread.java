package net.sf.sitemonitoring.service.check;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;

@Slf4j
public class XsdCheckThread extends AbstractSingleCheckThread {

	public XsdCheckThread(Check check, Map<URI, Object> visitedPagesGet, Map<URI, Object> visitedPagesHead) {
		super(check, visitedPagesGet, visitedPagesHead);
	}

	@Override
	public void performCheck() {
		log.debug("start perform check");
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = doGet(check.getUrl());
			if (httpResponse == null) {
				return;
			}
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				String webPage = EntityUtils.toString(entity);
				if (checkStatusCode(httpResponse, check.getUrl()) && check.getCondition() != null && !check.getCondition().isEmpty()) {
					InputStream xsdInputStream = null;
					try {
						xsdInputStream = FileUtils.openInputStream(new File(check.getCondition()));
						output = validateAgainstXSD(IOUtils.toInputStream(webPage), xsdInputStream, check.getUrl(), check.getCondition());
					} finally {
						if (xsdInputStream != null) {
							xsdInputStream.close();
						}
					}
				}
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
				output = "Unknown host: " + check.getUrl();
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

	private String validateAgainstXSD(InputStream xml, InputStream xsd, String checkUrl, String xsdFile) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(xsd));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(xml));
			return null;
		} catch (Exception ex) {
			return checkUrl + " doesn't match this XSD: " + xsdFile + ", error message: " + ex.getMessage();
		}
	}

}
