package net.sf.sitemonitoring.repository;

import java.util.Date;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.Check.IntervalType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface CheckRepository extends JpaRepository<Check, Integer> {

	@Modifying(clearAutomatically = true)
	@Query("update Check set startDate = current_timestamp, scheduledNextDate = ?1, checkState = 'RUNNING' where id = ?2")
	void startCheck(Date scheduledNextDate, int checkId);

	@Modifying(clearAutomatically = true)
	@Query("update Check c set c.endDate = current_timestamp, c.checkState = 'NOT_RUNNING' where c.id = ?1")
	void finishCheck(int checkId);

	@Modifying(clearAutomatically = true)
	@Query("update Check c set c.chartPeriodType = ?2, c.chartPeriodValue = ?3 where c.id = ?1")
	void updateChartInterval(int checkId, IntervalType chartPeriodType, int chartIntervalValue);

	@Modifying
	@Query("update Check c set scheduledNextDate = null where c.id = ?1")
	void startCheck(int checkId);

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Modifying
	@Query("update Check c set c.checkState = 'NOT_RUNNING', c.scheduledNextDate = null where c.checkState = 'RUNNING'")
	void resetAllChecks();

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Modifying
	@Query("update Check c set c.lastSentEmail = current_timestamp, currentErrorCount = 0 where c.id = ?1")
	void sendEmail(int checkId);

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Modifying
	@Query("update Check c set c.currentErrorCount = c.currentErrorCount + 1 where c.id = ?1")
	void incErrorCount(int checkId);

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Modifying
	@Query("update Check c set c.currentErrorCount = 0 where c.id = ?1")
	void clearErrorCount(int checkId);

	@Modifying
	@Query("update Check c set c.credentials = null where c.credentials.id = ?1")
	void removeCredentials(int id);

}
