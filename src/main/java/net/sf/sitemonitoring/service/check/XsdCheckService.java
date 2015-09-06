package net.sf.sitemonitoring.service.check;

import java.net.URI;
import java.util.HashMap;

import net.sf.sitemonitoring.entity.Check;

import org.springframework.stereotype.Service;

@Service
public class XsdCheckService extends AbstractCheckService {

	public String performCheck(Check check) {
		AbstractSingleCheckThread thread = new XsdCheckThread(check, new HashMap<URI, Object>(), new HashMap<URI, Object>());
		return startAndJoinThread(thread);
	}

}
