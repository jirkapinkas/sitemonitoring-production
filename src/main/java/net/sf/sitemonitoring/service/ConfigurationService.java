package net.sf.sitemonitoring.service;

import java.util.List;

import net.sf.sitemonitoring.entity.Configuration;
import net.sf.sitemonitoring.repository.ConfigurationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@CacheEvict(value = "configuration", allEntries = true)
	public void save(Configuration newConfiguration) {
		if (newConfiguration.getId() != 0) {
			// if user didn't change the password, it won't be changed in
			// database
			if (newConfiguration.getAdminPassword() == null || newConfiguration.getAdminPassword().isEmpty()) {
				Configuration originalConfiguration = find();
				newConfiguration.setAdminPassword(originalConfiguration.getAdminPassword());
			} else {
				// if the password has been changed, it needs to be encoded
				// using bcrypt
				newConfiguration.setAdminPassword(new BCryptPasswordEncoder().encode(newConfiguration.getAdminPassword()));
			}
			configurationRepository.save(newConfiguration);
		} else {
			if (configurationRepository.count() == 0) {
				configurationRepository.save(newConfiguration);
			} else {
				throw new UnsupportedOperationException("Cannot have more than one configuration");
			}
		}
	}

	@CacheEvict(value = "configuration", allEntries = true)
	public void saveExcludingPassword(Configuration newConfiguration) {
		newConfiguration.setAdminPassword(null);
		save(newConfiguration);
	}

	@Cacheable("configuration")
	public Configuration find() {
		List<Configuration> configurations = configurationRepository.findAll();
		if (configurations.size() == 0) {
			return null;
		} else if (configurations.size() > 1) {
			throw new UnsupportedOperationException("Cannot have more than one configuration");
		}
		return configurations.get(0);
	}
	
}
