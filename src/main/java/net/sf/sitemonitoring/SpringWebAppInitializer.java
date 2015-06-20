package net.sf.sitemonitoring;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.github.ziplet.filter.compression.CompressingFilter;

/**
 * This class replaces the need for web.xml for Spring bootstrap.
 * 
 */
public class SpringWebAppInitializer implements WebApplicationInitializer {

	private static final Logger logger = LoggerFactory.getLogger(SpringWebAppInitializer.class);

	@Override
	public void onStartup(ServletContext container) {
		// Create the 'root' Spring application context
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(SpringConfiguration.class);

		String springProfilesDefault = container.getInitParameter("spring.profiles.default");
		if ("dev".equals(springProfilesDefault) || "standalone".equals(springProfilesDefault)) {
			logger.info("Activating Compressing Filter");
			Dynamic compressingFilter = container.addFilter("CompressingFilter", CompressingFilter.class);
			compressingFilter.addMappingForUrlPatterns(null, true, "/*");
		} else {
			logger.info("Compressing Filter not activated, in order to have gzip compression, activate it either on Tomcat or Apache HTTPD");
		}

		// Manage the lifecycle of the root application context
		container.addListener(new ContextLoaderListener(rootContext));
	}

}
