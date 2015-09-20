package net.sf.sitemonitoring;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * This class replaces the need for web.xml for Spring bootstrap.
 * 
 */
@Slf4j
public class WebXmlCustomServer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) {
		// Create the 'root' Spring application context
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(Main.class);
		
		// Manage the lifecycle of the root application context
		servletContext.addListener(new ContextLoaderListener(rootContext));

		try {
			WebXmlCommon.initialize(servletContext, false);
		} catch (ServletException ex) {
			log.error("couldn't initialize WebXmlCommon", ex);
		}
	}

}
