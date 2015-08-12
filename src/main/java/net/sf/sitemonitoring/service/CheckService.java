package net.sf.sitemonitoring.service;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.IntervalType;
import net.sf.sitemonitoring.event.AbortCheckEvent;
import net.sf.sitemonitoring.repository.CheckRepository;
import net.sf.sitemonitoring.repository.CredentialsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.eventbus.EventBus;

@Slf4j
@Named
@Transactional
public class CheckService {

	@Autowired
	private CheckRepository checkRepository;
	
	@Autowired
	private CredentialsRepository credentialsRepository;

	@Autowired
	private EventBus eventBus;

	/**
	 * When this bean is created (during server startup) set
	 * CheckState.NOT_RUNNING to all checks
	 */
	@PostConstruct
	public void resetAllChecks() {
		checkRepository.resetAllChecks();
	}

	public void abort(int id, String reason) {
		log.debug("call eventbus to abort check " + id);
		eventBus.post(new AbortCheckEvent(id, reason));
	}

	public void save(Check check) {
		checkRepository.save(check);
	}

	public void delete(int id) {
		abort(id, "deleted");
		checkRepository.delete(id);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Check startCheck(Check check, Date scheduledNextDate) {
		checkRepository.startCheck(scheduledNextDate, check.getId());
		return checkRepository.findOne(check.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void finishCheck(Check check) {
		checkRepository.finishCheck(check.getId());
	}

	public void startNow(int checkId) {
		checkRepository.startCheck(checkId);
	}

	public List<Check> findAll() {
		return checkRepository.findAll(new Sort("id"));
	}
	
	public List<Check> findByPageId(Integer pageId) {
		if(pageId == null) {
			return checkRepository.findByPageIdIsNull(pageId, new Sort("id"));
		}
		return checkRepository.findByPageId(pageId, new Sort("id"));
	}

	public void updateChartInterval(int checkId, IntervalType chartIntervalType, int chartIntervalValue) {
		checkRepository.updateChartInterval(checkId, chartIntervalType, chartIntervalValue);
	}

	public Check findOne(int checkId) {
		return checkRepository.findOne(checkId);
	}

	@Transactional
	public void removeCredentials(int id) {
		checkRepository.removeCredentials(id);
		credentialsRepository.removeCredentials(id);
	}

	public void updateChartMaxMillis(int checkId, Integer chartMaxMillis) {
		checkRepository.updateChartMaxMillis(checkId, chartMaxMillis);
	}

}
