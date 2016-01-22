package net.sf.sitemonitoring.service.check;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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

    private static final String FILTER_COMMENT = "#";
    private static final String START_DIFF_MARKER = "<span style='background-color:yellow;'>";
    private static final String END_DIFF_MARKER = "</span>";

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

					if (check.isCheckForChanges()) {
                        String filteredWebPage = filterWebPage(check,webPage);
                        log.info("Check page for changes:" + check.getUrl());
                        String cachedWebPage = loadCachedWebPage(check);
                        updateCachedWebPage(check, filteredWebPage);
                        if (cachedWebPage != null) {
                            String pageDiff = diffWebPageChanged(filteredWebPage, cachedWebPage);
                            if (!StringUtils.isEmpty(pageDiff)) {
                                log.info("PageChanged:" + check.getUrl());
                                appendMessage("Page content is changed:\n"+pageDiff);
                            } else {
                                log.info("PageUnChanged:" + check.getUrl());
                            }
                        } else {
                            log.info("First check, can't find changes at this time because no cached page is available:" + check.getUrl());
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
			output = check.getUrl() + " has error: error downloading: " + check.getUrl() + " exception: " + ex.getClass().getName();
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

	private String diffWebPageChanged(String webPage, String cachedWebPage) {
        StringBuffer diffBuf = new StringBuffer();
        try (BufferedReader br1 = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(webPage.getBytes())));
                BufferedReader br2 = new BufferedReader(
                        new InputStreamReader(new ByteArrayInputStream(cachedWebPage.getBytes())))) {
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            int count = 0;
            while (line1 != null) {
                if (line2!=null && !line1.trim().equals(line2.trim())) {
                    line1 = StringEscapeUtils.escapeHtml(line1);
                    line2 = StringEscapeUtils.escapeHtml(line2);
                    count++;
                    diffBuf.append("<p>Diff ["+count+"]:");
                    diffBuf.append("<ul>");
                    boolean marked = false;
                    for(int n=0 ; n< line1.length() && n<line2.length() ; n++) {
                        if (line1.charAt(n) != line2.charAt(n)) {
                            line1 = line1.substring(0,n)+START_DIFF_MARKER+line1.substring(n);
                            line2 = line2.substring(0,n)+START_DIFF_MARKER+line2.substring(n);
                            marked = true;
                            break;
                        }
                    }
                    if (!marked) {
                        int l1 = line1.length();
                        int l2 = line2.length();
                        if (l1 < l2) {
                            line2 = line2.substring(0,l1)+START_DIFF_MARKER+line2.substring(l1);
                        } else {
                            line1 = line1.substring(0,l2)+START_DIFF_MARKER+line1.substring(l2);
                        }
                    }

                    int l1 = line1.length();
                    int l2=line2.length();
                    marked = false;
                    for(int n=0 ; n < l1 && n < l2 ; ) {
                        n++;
                        if (line1.charAt(l1-n) != line2.charAt(l2-n)) {
                            line1 = line1.substring(0,l1-n+1)+END_DIFF_MARKER+line1.substring(l1-n+1);
                            line2 = line2.substring(0,l2-n+1)+END_DIFF_MARKER+line2.substring(l2-n+1);
                            marked=true;
                            break;
                        }
                    }
                    if (!marked) {
                        if (l1 < l2) {
                            line2 = line2.substring(0,l1+1)+END_DIFF_MARKER+line2.substring(l1+1);
                        } else {
                            line1 = line1.substring(0,l2+1)+END_DIFF_MARKER+line1.substring(l2+1);
                        }
                    }

                    diffBuf.append("<li/> "+line1);
                    diffBuf.append("<li/> "+line2);
                    diffBuf.append("</ul>");
                    diffBuf.append("</ul>");
                }
                line1 = br1.readLine();
                line2 = br2.readLine();
            }
        } catch (IOException e) {
            log.error("Error filtering webPage", e);
        }
        return diffBuf.toString();
    }

	private String globalFilterWebPage(Check check, String webPage, String globalFilter) {
        String url = check.getUrl();
        Pattern p = Pattern.compile(globalFilter, Pattern.DOTALL);
        Matcher m = p.matcher(webPage);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        String filteredWebPage = sb.toString();
        return filteredWebPage;
    }
        
    private String filterWebPage(Check check, String webPage) {        
        // Read filters
        String filterSet = check.getCheckForChangesFilter();
        String[] filters = null;
        if (filterSet!=null) {
            if (filterSet.contains("\n")) {
                filters = filterSet.toLowerCase().split("\n");
            } else if (filterSet.contains("|")) {
                filters = filterSet.toLowerCase().split("\\|");
            }
        } else {
            filters = new String[]{};
        }
        
        // Apply filters
        String filteredWebPage = webPage;
        for(String filter : filters) {
            if (!filter.startsWith(FILTER_COMMENT)) {
                filteredWebPage = globalFilterWebPage(check,filteredWebPage,filter);
            }
        }
        
        return filteredWebPage;
    }

	private String loadCachedWebPage(Check check) {
		File webPageCacheFile = getWebPageCacheFile(check);
		log.error("loadCachedWebPage from :" + webPageCacheFile.getAbsolutePath());
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(webPageCacheFile)))) {
			if (webPageCacheFile.exists()) {
				StringBuffer sb = new StringBuffer();
				String line = br.readLine();
				while (line != null) {
					sb.append(line + "\n");
					line = br.readLine();
				}
				return sb.toString();
			}
		} catch (Exception e) {
			log.error("Couldn't read cached webpage from:" + webPageCacheFile.getAbsolutePath());
		}
		return null;
	}

	private void updateCachedWebPage(Check check, String webPage) {
		File webPageCacheFile = getWebPageCacheFile(check);
		File webPageCacheBakFile = getWebPageCacheBakFile(check);
		webPageCacheFile.renameTo(webPageCacheBakFile);
		log.error("saveCachedWebPage to :" + webPageCacheFile.getAbsolutePath());
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(webPageCacheFile)));
				BufferedReader br = new BufferedReader(
						new InputStreamReader(new ByteArrayInputStream(webPage.getBytes())))) {
			String line = br.readLine();
			while (line != null) {
				bw.write(line + "\n");
				line = br.readLine();
			}
		} catch (Exception e) {
			log.error("Erro saving webPageCache file:" + webPageCacheFile.getAbsolutePath(), e);
		}
	}

	private File getWebPageCacheFile(Check check) {
		String property = "java.io.tmpdir";
		String tempDirPath = System.getProperty(property);
		File tempDir = new File(tempDirPath);
		try {
		    if (!tempDir.exists()) {
		    	FileUtils.forceMkdir(tempDir);
		    }
		} catch (IOException e) {
			log.error("Couldn't create tempDir:"+e.getMessage(),e);
		}
		File webPageCacheFile = new File(tempDir, "webPageCache_" + check.getId());
		return webPageCacheFile;
	}

	private File getWebPageCacheBakFile(Check check) {
		String property = "java.io.tmpdir";
		String tempDir = System.getProperty(property);
		File webPageCacheFile = new File(tempDir, "webPageCache_" + check.getId() + "_bak");
		return webPageCacheFile;
	}
}
