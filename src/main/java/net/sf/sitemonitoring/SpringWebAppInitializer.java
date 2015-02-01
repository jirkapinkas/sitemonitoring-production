package net.sf.sitemonitoring;

import javax.servlet.ServletContext;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * This class replaces the need for web.xml for Spring bootstrap.
 * 
 */
public class SpringWebAppInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext container) {
		// Create the 'root' Spring application context
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(SpringConfiguration.class);

		// Manage the lifecycle of the root application context
		container.addListener(new ContextLoaderListener(rootContext));
	}

}
