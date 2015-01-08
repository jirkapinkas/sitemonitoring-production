package net.sf.sitemonitoring.service.check;

import net.sf.sitemonitoring.entity.Check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpiderCheckService extends AbstractCheckService {

	@Autowired
	private SinglePageCheckService singlePageCheckService;

	public String performCheck(Check spiderCheck) {
		throw new UnsupportedOperationException();
//		SpiderCheckThread thread = new SpiderCheckThread(singlePageCheckService, spiderCheck);
//		return startAndJoinThread(thread);
	}

}
