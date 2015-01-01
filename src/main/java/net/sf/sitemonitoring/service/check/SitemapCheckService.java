package net.sf.sitemonitoring.service.check;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.jaxb.sitemap.Url;
import net.sf.sitemonitoring.jaxb.sitemap.Urlset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SitemapCheckService extends AbstractCheckService {

	private static JAXBContext jaxbContext;

	@Autowired
	private SinglePageCheckService singlePageCheckService;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(Urlset.class, Url.class);
		} catch (JAXBException e) {
			log.error("Cannot create instance of Urlset JAXBContext", e);
		}
	}

	public String performCheck(Check sitemapCheck) {
		SitemapCheckThread thread = new SitemapCheckThread(jaxbContext, singlePageCheckService, sitemapCheck);
		return startAndJoinThread(thread);
	}

}
