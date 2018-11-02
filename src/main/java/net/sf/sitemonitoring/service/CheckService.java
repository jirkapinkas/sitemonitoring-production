package net.sf.sitemonitoring.service;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.eventbus.EventBus;

import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.IntervalType;
import net.sf.sitemonitoring.event.AbortCheckEvent;
import net.sf.sitemonitoring.repository.CheckRepository;
import net.sf.sitemonitoring.repository.CredentialsRepository;

@Slf4j
@Component
@Transactional
public class CheckService {

	@Autowired
	private CheckRepository checkRepository;

	@Autowired
	private CredentialsRepository credentialsRepository;

	@Autowired
	private EventBus eventBus;
	
	 @PersistenceContext
	 private EntityManager entityManager;

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

	/**
	 * Save check. If check is ranged, multiple checks will be saved.
	 * @param check
	 */
	@Transactional
	public void save(Check check) {
		if(isRangeCheck(check)) {
			int low = getLowRange(check);
			int high = getHighRange(check);
			String originalName = check.getName();
			String originalUrl = check.getUrl();
			for(int i = low; i <= high; i++) {
				check.setName(originalName);
				check.setUrl(originalUrl);
				replaceRange(check, i, low, high);
				check.setId(0);
				entityManager.persist(check);
				entityManager.flush();
				entityManager.clear();
			}
		} else {
			checkRepository.save(check);
		}
	}
	
	protected void replaceRange(Check check, int index, int low, int high) {
		check.setName(check.getName().replace("[" + low + ".." + high + "]", Integer.toString(index)));
		check.setUrl(check.getUrl().replace("[" + low + ".." + high + "]", Integer.toString(index)));
	}
	
	protected int getLowRange(Check check) {
		return Integer.parseInt(check.getName().split("\\[")[1].split("\\.\\.")[0]);
	}
	
	protected int getHighRange(Check check) {
		return Integer.parseInt(check.getName().split("\\]")[0].split("\\.\\.")[1]);
	}
	
	protected boolean isRangeCheck(Check check) {
		if(check.getName() != null && check.getUrl() != null) {
			return check.getName().matches(".*\\[\\d*\\.\\.\\d*\\].*") && check.getUrl().matches(".*\\[\\d*\\.\\.\\d*\\].*");
		}
		return false;
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
		if (pageId == null) {
			return checkRepository.findByPageIdIsNull(new Sort("id"));
		}
		if (pageId <= 0) {
			return checkRepository.findByPageIdIsNull(new Sort("id"));
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

	public void deleteAll() {
		checkRepository.deleteAll();
	}

}
