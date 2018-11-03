package net.sf.sitemonitoring.service.check;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SinglePageCheckService extends AbstractCheckService {

	public String performCheck(Check check, Map<URI, Object> visitedPagesGet, Map<URI, Object> visitedPagesHead) {
		AbstractSingleCheckThread thread = new SinglePageCheckThread(check, visitedPagesGet, visitedPagesHead);
		return startAndJoinThread(thread);
	}

	public String performCheck(Check check) {
		AbstractSingleCheckThread thread = new SinglePageCheckThread(check, new HashMap<>(), new HashMap<>());
		return startAndJoinThread(thread);
	}

	public static boolean ignoreUrl(String url, String excludedUrls) {
		if (excludedUrls != null && !excludedUrls.isEmpty()) {
			String[] split = excludedUrls.split("\r\n");
			log.debug("excluded urls: " + excludedUrls);
			for (String string : split) {
				string = string.trim();
				if (string.isEmpty()) {
					continue;
				}

				String pattern = string.replaceAll("\\*", "\\.\\*");
				log.debug("test exclude pattern " + pattern + " on url: " + url);
				if (url.matches(pattern)) {
					log.debug("pattern matches");
					return true;
				} else {
					log.debug("patern doesn't match");
				}
			}
		}
		return false;
	}

	public static boolean isSameDomain(String url1, String url2) {
		if (url1 == null || url2 == null) {
			return false;
		}
		try {
			URI uri1 = new URI(url1);
			URI uri2 = new URI(url2);
			if(uri1.getHost() == null || uri2.getHost() == null) {
				return false;
			}
			String domain1 = uri1.getHost().replace("www.", "");
			String domain2 = uri2.getHost().replace("www.", "");
			return domain1.equals(domain2);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
