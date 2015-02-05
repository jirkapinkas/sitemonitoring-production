package net.sf.sitemonitoring.service.check;

import java.net.URI;
import java.util.HashMap;

import net.sf.sitemonitoring.entity.Check;

import org.springframework.stereotype.Service;

@Service
public class JsonCheckService extends AbstractCheckService {

	public String performCheck(Check check) {
		AbstractSingleCheckThread thread = new JsonCheckThread(check, new HashMap<URI, Object>(), new HashMap<URI, Object>());
		return startAndJoinThread(thread);
	}

}
