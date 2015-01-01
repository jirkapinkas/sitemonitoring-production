package net.sf.sitemonitoring.push;

import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/running")
public class MonitoringStatusResource {

	@OnMessage(encoders = { JSONEncoder.class })
	public Boolean onMessage(Boolean result) {
		return result;
	}
}
