package net.sf.sitemonitoring.service.check;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
public class SpiderCheckThread extends AbstractCheckThread {

	private SinglePageCheckService singlePageCheckService;

	public SpiderCheckThread(SinglePageCheckService singlePageCheckService, Check spiderCheck) {
		super(spiderCheck);
		this.singlePageCheckService = singlePageCheckService;
	}

	protected void findUrls(String referer, String htmlPage, Map<String, String> allPages) {
		log.debug("find urls on this web page: " + referer);
		if (abort) {
			appendMessage("aborted");
			return;
		}
		Document document = Jsoup.parse(htmlPage);
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
			log.debug("spider check found url: " + url);
			if (!url.toString().isEmpty() && !url.startsWith("mailto:") && !SinglePageCheckService.ignoreUrl(url, check.getDoNotFollowUrls()) && url.startsWith(check.getUrl()) && !url.equals(referer)) {
				log.debug("spider check put to all pages url: " + url);
				allPages.put(url, referer);
			}
		}
	}

	private String checkHomepage(Map<URI, Object> visitedPagesGet, Map<URI, Object> visitedPagesHead, Map<String, String> allPages, Map<String, String> pagesVisitedBySpider) {
		Check singleCheck = new Check();
		singleCheck.setId(check.getId());
		singleCheck.setCondition(check.getCondition());
		singleCheck.setConditionType(check.getConditionType());
		singleCheck.setReturnHttpCode(check.getReturnHttpCode());
		singleCheck.setUrl(check.getUrl());
		singleCheck.setDoNotFollowUrls(check.getDoNotFollowUrls());
		singleCheck.setCheckBrokenLinks(check.isCheckBrokenLinks());
		singleCheck.setConnectionTimeout(check.getConnectionTimeout());
		singleCheck.setSocketTimeout(check.getSocketTimeout());
		singleCheck.setStoreWebpage(true);
		String result = singlePageCheckService.performCheck(singleCheck, visitedPagesGet, visitedPagesHead);
		findUrls(singleCheck.getUrl(), singleCheck.getWebPage(), allPages);
		allPages.put(check.getUrl(), check.getUrl());
		pagesVisitedBySpider.put(check.getUrl(), null);
		return result;
	}

	@Override
	public void performCheck() {
		Map<URI, Object> visitedPagesGet = new HashMap<URI, Object>();
		Map<URI, Object> visitedPagesHead = new HashMap<URI, Object>();
		// key = page URL, value = referer (page, which contains key)
		Map<String, String> allPages = new HashMap<String, String>();
		Map<String, String> pagesVisitedBySpider = new HashMap<String, String>();

		log.debug("spider performCheck() start");

		String homepageResult = checkHomepage(visitedPagesGet, visitedPagesHead, allPages, pagesVisitedBySpider);
		if (homepageResult != null && !homepageResult.isEmpty()) {
			output = homepageResult;
			return;
		}

		StringBuilder stringBuilder = new StringBuilder();
		while (pagesVisitedBySpider.size() != allPages.size()) {

			System.out.println("spider stale bezi");

			HashSet<String> set = new HashSet<String>(allPages.keySet());
			for (String url : set) {
				if (abort) {
					output = "aborted";
					return;
				}
				if (SinglePageCheckService.ignoreUrl(url, check.getExcludedUrls())) {
					log.debug("ignore url: " + url);
					continue;
				}
				if (pagesVisitedBySpider.containsKey(url)) {
					continue;
				}
				Check singleCheck = new Check();
				singleCheck.setId(check.getId());
				singleCheck.setCondition(check.getCondition());
				singleCheck.setConditionType(check.getConditionType());
				singleCheck.setReturnHttpCode(check.getReturnHttpCode());
				singleCheck.setUrl(url);
				singleCheck.setDoNotFollowUrls(check.getDoNotFollowUrls());
				singleCheck.setCheckBrokenLinks(check.isCheckBrokenLinks());
				singleCheck.setConnectionTimeout(check.getConnectionTimeout());
				singleCheck.setSocketTimeout(check.getSocketTimeout());
				singleCheck.setStoreWebpage(true);
				String checkResultTxt = singlePageCheckService.performCheck(singleCheck, visitedPagesGet, visitedPagesHead);
				findUrls(url, singleCheck.getWebPage(), allPages);
				pagesVisitedBySpider.put(singleCheck.getUrl(), null);
				if (checkResultTxt != null) {
					stringBuilder.append(allPages.get(url) + " has error: " + checkResultTxt);
					stringBuilder.append("<br />");
				}
			}
		}

		if (stringBuilder.length() != 0) {
			output = stringBuilder.toString();
		}
		log.debug("sitemap performCheck() finish");
	}
}
