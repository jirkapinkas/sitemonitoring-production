package net.sf.sitemonitoring.service.check;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.event.AbortCheckEvent;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.common.eventbus.Subscribe;

@Slf4j
public abstract class AbstractCheckThread extends Thread {

	protected boolean abort = false;

	protected String output;

	protected Check check;

	protected CloseableHttpClient httpClient;
	
	public AbstractCheckThread(Check check) {
		this.check = check;
	}

	@Subscribe
	public void abort(AbortCheckEvent abortCheckEvent) throws IOException {
		log.debug("called abort " + abortCheckEvent.getCheckId());
		if (check.getId() == abortCheckEvent.getCheckId()) {
			log.debug("aborted check " + check.getId());
			abort = true;
			httpClient.close();
		}
	}
	
	public abstract void performCheck();

	public void run() {
		httpClient = HttpClients.createDefault();
		try {
			performCheck();
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				log.error("Error closing HTTP client", e);
			}
		}
	}

	public String getOutput() {
		return output;
	}

	protected void appendMessage(String message) {
		if (output == null) {
			output = "";
		}
		if (message != null && !message.trim().isEmpty()) {
			output += message;
		}
	}

}
