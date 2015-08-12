package net.sf.sitemonitoring.repository;

import java.util.List;

import net.sf.sitemonitoring.entity.Page;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PageRepository extends JpaRepository<Page, Integer> {

	@Query("select distinct p from Page p left join fetch p.checks")
	List<Page> findAllFetchChecks();

}
