package net.sf.sitemonitoring.repository;

import net.sf.sitemonitoring.entity.Configuration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {

}
