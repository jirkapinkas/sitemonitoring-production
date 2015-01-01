package net.sf.sitemonitoring.service.check;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.EventBus;

@Slf4j
public abstract class AbstractCheckService {

	@Autowired
	protected EventBus eventBus;

	protected String startAndJoinThread(AbstractCheckThread thread) {
		try {
			eventBus.register(thread);
			thread.start();
			thread.join();
		} catch (InterruptedException e) {
			log.error("interrupted check url thread", e);
		} finally {
			eventBus.unregister(thread);
		}
		return thread.getOutput();
	}

	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

}
