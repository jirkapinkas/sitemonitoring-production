package net.sf.sitemonitoring.service.check;

import java.net.URI;
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
		AbstractSingleCheckThread thread = new SinglePageCheckThread(check, new HashMap<URI, Object>(), new HashMap<URI, Object>());
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

}
