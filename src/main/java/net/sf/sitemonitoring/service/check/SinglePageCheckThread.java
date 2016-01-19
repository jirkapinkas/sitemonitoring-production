package net.sf.sitemonitoring.service.check;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.xml.XmlEscapers;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.CheckType;
import net.sf.sitemonitoring.entity.Check.HttpMethod;

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
                    if (check.isCheckForChanges()) {
                        log.info("Check page for changes:" + check.getUrl());
                        String cachedWebPage = loadCachedWebPage(check);
                        updateCachedWebPage(check, webPage);
                        if (cachedWebPage != null && !webPage.equals(cachedWebPage)) {
                            log.info("PageChanged:" + check.getUrl());
                            appendMessage("Page content is changed");
                        } else {
                            log.info("First Run or PageUnChanged:" + check.getUrl());
                        }

                    }
                    if (check.isStoreWebpage()) {
                        check.setWebPage(webPage);
                    }
                    if (checkStatusCode(httpResponse, check.getUrl()) && check.getCondition() != null
                            && !check.getCondition().isEmpty()) {
                        switch (check.getConditionType()) {
                        case CONTAINS:
                            if (!webPage.contains(check.getCondition())) {
                                appendMessage(check.getUrl() + " doesn't contain "
                                        + XmlEscapers.xmlContentEscaper().escape(check.getCondition()));
                            }
                            break;
                        case DOESNT_CONTAIN:
                            if (webPage.contains(check.getCondition())) {
                                appendMessage(check.getUrl() + " contains "
                                        + XmlEscapers.xmlContentEscaper().escape(check.getCondition()));
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

                            if (!url.isEmpty() && !url.startsWith("mailto:")
                                    && !SinglePageCheckService.ignoreUrl(url, check.getDoNotFollowUrls())) {
                                boolean skip = false;
                                if (check.getFollowOutboundBrokenLinks() == null
                                        || check.getFollowOutboundBrokenLinks() == false) {
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
                                    SinglePageCheckThread checkThread = new SinglePageCheckThread(subCheck,
                                            visitedPagesGet, visitedPagesHead);
                                    log.debug("check sub-link: " + subCheck.getUrl());
                                    checkThread.start();
                                    checkThread.join();
                                    if (checkThread.getOutput() != null && !checkThread.getOutput().trim().isEmpty()) {
                                        appendMessage(
                                                check.getUrl() + " has error: " + checkThread.getOutput() + "<br />");
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
            output = check.getUrl() + " has error: incorrect URL";
            log.debug(output, ex);
        } catch (ConnectTimeoutException ex) {
            output = check.getUrl() + " has error: connect timeout";
            log.debug(output, ex);
        } catch (SocketTimeoutException ex) {
            output = check.getUrl() + " has error: socket timeout";
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
            output = check.getUrl() + " has error: error downloading: " + check.getUrl() + " exception: "
                    + ex.getClass().getName();
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

    private String loadCachedWebPage(Check check) {
        File webPageCacheFile = getWebPageCacheFile(check);
        log.error("loadCachedWebPage from :"+webPageCacheFile.getAbsolutePath());
        try {
            if (webPageCacheFile.exists()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(webPageCacheFile)));
                StringBuffer sb = new StringBuffer();
                String line = br.readLine();
                while(line!=null) {
                    sb.append(line+"\n");
                    line = br.readLine();
                }
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateCachedWebPage(Check check, String webPage) {
        File webPageCacheFile = getWebPageCacheFile(check);
        log.error("saveCachedWebPage to :"+webPageCacheFile.getAbsolutePath());
        try (FileOutputStream fout = new FileOutputStream(webPageCacheFile)) {
            fout.write(webPage.getBytes());
        } catch (Exception e) {
            log.error("Erro saving webPageCache file:"+webPageCacheFile.getAbsolutePath(), e);
        }
    }

    private File getWebPageCacheFile(Check check) {
        String property = "java.io.tmpdir";
        String tempDir = System.getProperty(property);
        File webPageCacheFile = new File(tempDir, "webPageCache_"+check.getId());
        return webPageCacheFile;
    }
}
