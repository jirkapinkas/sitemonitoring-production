package net.sf.sitemonitoring;

import com.google.common.eventbus.EventBus;
import net.sf.sitemonitoring.annotation.ViewScope;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@EnableScheduling
@EnableAsync
@EnableCaching
@SpringBootApplication
public class Application implements AsyncConfigurer {

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
        cacheManager.setCaches(Collections.singletonList(new ConcurrentMapCache("configuration")));
        return cacheManager;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public static CustomScopeConfigurer viewScopeConfigurer() {
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
        Map scopes = new HashMap();
        scopes.put("view", new ViewScope());
        customScopeConfigurer.setScopes(scopes);
        return customScopeConfigurer;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).headless(false).run(args);
    }

}
