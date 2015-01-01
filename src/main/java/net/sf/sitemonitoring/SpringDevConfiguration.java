package net.sf.sitemonitoring;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import net.sf.sitemonitoring.annotation.DevProfile;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@DevProfile
@Configuration
public class SpringDevConfiguration {

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(dataSource);
		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
		jpaProperties.put("hibernate.show_sql", "true");
		// jpaProperties.put("hibernate.format_sql", "true");
		entityManagerFactory.setJpaProperties(jpaProperties);
		entityManagerFactory.setPackagesToScan("net.sf.sitemonitoring.entity");
		entityManagerFactory.setPersistenceProvider(new HibernatePersistenceProvider());
		return entityManagerFactory;
	}

	@Bean
	public JpaTransactionManager transactionManager(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
		transactionManager.setDataSource(dataSource);
		return transactionManager;
	}

	@Bean
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:hsqldb:mem:test");
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		return dataSource;
	}

}
