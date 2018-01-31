package net.sf.sitemonitoring;

import net.sf.sitemonitoring.annotation.MyServerProfile;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@MyServerProfile
@SpringBootApplication
public class MainMyServer {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.show_sql", "false");
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

    public static void main(String[] args) {
        new SpringApplicationBuilder(MainMyServer.class).headless(false).run(args);
    }

}
