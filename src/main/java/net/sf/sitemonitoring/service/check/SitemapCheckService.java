package net.sf.sitemonitoring.service.check;

import net.sf.sitemonitoring.entity.Check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SitemapCheckService extends AbstractCheckService {

	@Autowired
	private SinglePageCheckService singlePageCheckService;

	public String performCheck(Check sitemapCheck) {
		SitemapCheckThread thread = new SitemapCheckThread(singlePageCheckService, sitemapCheck);
		return startAndJoinThread(thread);
	}

}
