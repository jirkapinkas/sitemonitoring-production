package net.sf.sitemonitoring.repository;

import java.util.Date;
import java.util.List;

import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.CheckResult;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CheckResultRepository extends JpaRepository<CheckResult, Integer> {

	List<CheckResult> findByCheck(Check check, Pageable pageable);

	@Modifying
	@Query("delete from CheckResult c where c.check.id = ?1 and c.startTime < ?2")
	void deleteOld(int checkId, Date date);

	@Query("select c from CheckResult c where c.check.id = ?1 and c.startTime > ?2 and c.startTime < ?3 order by id desc")
	List<CheckResult> findByCheckIdDateRange(int checkId, Date dateFrom, Date dateTo);

	@Query("select max(c.responseTime) from CheckResult c where c.check.id = ?1")
	Integer findMaxMillis(int checkId);

	@Query("select c from CheckResult c where c.check.id = ?1 and c.success = false")
	List<CheckResult> findByCheckIdErrors(int checkId);
	
	@Modifying
	@Query("delete from CheckResult c where c.check.id = ?1")
	void deleteResults(int checkId);

}
