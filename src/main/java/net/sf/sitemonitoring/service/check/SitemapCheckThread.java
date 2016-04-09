package net.sf.sitemonitoring.service.check;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.HttpMethod;
import net.sf.sitemonitoring.jaxb.sitemap.Url;
import net.sf.sitemonitoring.jaxb.sitemap.Urlset;
import net.sf.sitemonitoring.jaxb.sitemapindex.Sitemap;
import net.sf.sitemonitoring.jaxb.sitemapindex.Sitemapindex;

@Slf4j
public class SitemapCheckThread extends AbstractCheckThread {

	private static JAXBContext jaxbContextSitemap;

	private static JAXBContext jaxbContextSitemapIndex;

	static {
		try {
			jaxbContextSitemap = JAXBContext.newInstance(Urlset.class, Url.class);
		} catch (JAXBException e) {
			log.error("Cannot create an instance of Urlset JAXBContext", e);
		}
		try {
			jaxbContextSitemapIndex = JAXBContext.newInstance(Sitemapindex.class, Sitemap.class);
		} catch (JAXBException e) {
			log.error("Cannot create an instance of Sitemapindex JAXBContext", e);
		}
	}

	private SinglePageCheckService singlePageCheckService;

	public SitemapCheckThread(SinglePageCheckService singlePageCheckService, Check check) {
		super(check);
		this.singlePageCheckService = singlePageCheckService;
	}
	
	/**
	 * Determines if a byte array is compressed. The java.util.zip GZip
	 * implementaiton does not expose the GZip header so it is difficult to determine
	 * if a string is compressed.
	 * 
	 * @param bytes an array of bytes
	 * @return true if the array is compressed or false otherwise
	 * @throws java.io.IOException if the byte array couldn't be read
	 */
	 public boolean isCompressed(byte[] bytes) throws IOException
	 {
	      if ((bytes == null) || (bytes.length < 2))
	      {
	           return false;
	      }
	      else
	      {
	            return ((bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)));
	      }
	 }
	 
	 /**
	  * Unzip byte array
	  * @param bytes
	  * @throws IOException
	  */
    public byte[] gunzipIt(byte[] bytes) throws IOException{
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try(GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(bytes))){
           int len;
           while ((len = gzis.read(buffer)) > 0) {
           	out.write(buffer, 0, len);
           }
       }
        return out.toByteArray();
      }


	protected String downloadSitemap(CloseableHttpClient httpClient, String url) throws IOException {
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(new HttpGet(url));
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				throw new IOException();
			}
			HttpEntity entity = httpResponse.getEntity();
			byte[] bytes = EntityUtils.toByteArray(entity);
			if(isCompressed(bytes)) {
				bytes = gunzipIt(bytes);
			}
			return new String(bytes);
		} catch (IllegalArgumentException ex) {
			throw new IOException("Error downloading sitemap: " + url + " incorrect URL", ex);
		} catch (ConnectTimeoutException ex) {
			throw new IOException("Error downloading sitemap: " + url + " connect timeout", ex);
		} catch (SocketTimeoutException ex) {
			throw new IOException("Error downloading sitemap: " + url + " socket timeout", ex);
		} catch (UnknownHostException ex) {
			try {
				throw new IOException("Error downloading sitemap: " + url + " Cannot connect to: " + new URI(url).getHost(), ex);
			} catch (URISyntaxException e) {
				throw new IOException("Error downloading sitemap: " + url + " Cannot connect to: " + url, ex);
			}
		} catch (HttpHostConnectException ex) {
			try {
				throw new IOException("Error downloading sitemap: " + url + " Cannot connect to: " + new URI(url).getHost(), ex);
			} catch (URISyntaxException e) {
				throw new IOException("Error downloading sitemap: " + url + " Cannot connect to: " + url, ex);
			}
		} catch (IOException ex) {
			throw new IOException("Error downloading sitemap: " + url + " exception: " + ex.getClass().getName(), ex);
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
			return (Urlset) jaxbContextSitemap.createUnmarshaller().unmarshal(inputStream);
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

	protected Sitemapindex readSitemapIndex(String sitemapIndexXml) throws JAXBException {
		InputStream inputStream = null;
		try {
			inputStream = IOUtils.toInputStream(sitemapIndexXml, "UTF-8");
			return (Sitemapindex) jaxbContextSitemapIndex.createUnmarshaller().unmarshal(inputStream);
		} catch (JAXBException e) {
			throw new JAXBException("Error reading sitemap index", e);
		} catch (IOException e) {
			throw new JAXBException("Cannot convert sitemap index using UTF-8", e);
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
			singleCheck.setUrl(url.getLoc().trim());
			singleCheck.setDoNotFollowUrls(sitemapCheck.getDoNotFollowUrls());
			singleCheck.setCheckBrokenLinks(sitemapCheck.isCheckBrokenLinks());
			singleCheck.setFollowOutboundBrokenLinks(sitemapCheck.getFollowOutboundBrokenLinks());
			if ((sitemapCheck.getCondition() != null && !sitemapCheck.getCondition().isEmpty()) || check.isCheckBrokenLinks()) {
				singleCheck.setHttpMethod(HttpMethod.GET);
			} else {
				singleCheck.setHttpMethod(HttpMethod.HEAD);
			}
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
		Map<URI, Object> visitedPagesGet = new HashMap<>();
		Map<URI, Object> visitedPagesHead = new HashMap<>();
		log.debug("sitemap performCheck() start");
		try {
			String sitemapXml = downloadSitemap(httpClient, check.getUrl());
			if (sitemapXml.indexOf("</sitemapindex>") != -1) {
				// it's sitemapindex
				Sitemapindex sitemapindex = readSitemapIndex(sitemapXml);
				log.debug("sitemap index contains " + sitemapindex.getSitemaps().size() + " sitemaps");
				StringBuilder outputStringBuilder = new StringBuilder();
				for (Sitemap sitemap : sitemapindex.getSitemaps()) {
					String realSitemapXml = downloadSitemap(httpClient, sitemap.getLoc());
					Urlset urlset = readSitemap(realSitemapXml);
					log.debug("sitemap contains " + urlset.getUrls().size() + " urls");
					String realSitemapOutput = check(urlset, check, visitedPagesGet, visitedPagesHead);
					if (realSitemapOutput != null) {
						outputStringBuilder.append(realSitemapOutput);
					}
				}
				if (!outputStringBuilder.toString().trim().isEmpty()) {
					output = outputStringBuilder.toString();
				}
			} else {
				// it's sitemap
				Urlset urlset = readSitemap(sitemapXml);
				log.debug("sitemap contains " + urlset.getUrls().size() + " urls");
				output = check(urlset, check, visitedPagesGet, visitedPagesHead);
			}
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
