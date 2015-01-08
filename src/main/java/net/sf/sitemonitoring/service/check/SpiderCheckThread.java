package net.sf.sitemonitoring.service.check;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;

@Slf4j
public class SpiderCheckThread extends AbstractCheckThread {

	private SinglePageCheckService singlePageCheckService;

	public SpiderCheckThread(SinglePageCheckService singlePageCheckService, Check spiderCheck) {
		this.check = spiderCheck;
		this.singlePageCheckService = singlePageCheckService;
	}
	
	protected List<URL> findUrls(String htmlPage) {
		return null;
	}

	@Override
	public void performCheck() {
		Map<URI, Object> visitedPages = new HashMap<URI, Object>();
		Map<URI, Object> visitedPagesSpider = new HashMap<URI, Object>();
		log.debug("spider performCheck() start");
		StringBuilder stringBuilder = new StringBuilder();
		
		List<URL> urls = findUrls(check.getUrl());
		

		output = stringBuilder.toString();
		log.debug("sitemap performCheck() finish");
	}

}
