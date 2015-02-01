package net.sf.sitemonitoring.repository;

import net.sf.sitemonitoring.entity.Credentials;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CredentialsRepository extends JpaRepository<Credentials, Integer> {

	@Modifying
	@Query("delete from Credentials where id = ?1")
	void removeCredentials(int id);

}
