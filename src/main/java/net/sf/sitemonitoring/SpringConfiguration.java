package net.sf.sitemonitoring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import net.sf.sitemonitoring.annotation.ViewScope;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.google.common.eventbus.EventBus;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
@EnableCaching
public class SpringConfiguration implements AsyncConfigurer {

	@Bean
	public EventBus eventBus() {
		return new EventBus();
	}

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(10);
		executor.setCorePoolSize(5);
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return null;
	}

	@Bean
	public CacheManager cacheManager() {
		// configure and return an implementation of Spring's CacheManager SPI
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("configuration")));
		return cacheManager;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public CustomScopeConfigurer viewScopeConfigurer() {
		CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
		Map scopes = new HashMap();
		scopes.put("view", new ViewScope());
		customScopeConfigurer.setScopes(scopes);
		return customScopeConfigurer;
	}

}
