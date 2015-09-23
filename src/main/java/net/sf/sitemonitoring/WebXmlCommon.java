package net.sf.sitemonitoring;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionTrackingMode;

import net.sf.sitemonitoring.servlet.StartBrowserServlet;

import com.google.common.collect.ImmutableSet;
import com.sun.faces.config.FacesInitializer;

public final class WebXmlCommon {

	public static void initialize(ServletContext servletContext, boolean dev) throws ServletException {
		FacesInitializer facesInitializer = new FacesInitializer();

		servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");
		servletContext.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", "true");
		if (dev) {
			servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "0");
			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
		} else {
			servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "-1");
			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Production");
		}
		servletContext.setSessionTrackingModes(ImmutableSet.of(SessionTrackingMode.COOKIE));

		Set<Class<?>> clazz = new HashSet<Class<?>>();
		clazz.add(WebXmlSpringBoot.class);
		facesInitializer.onStartup(clazz, servletContext);

		Dynamic startBrowserServlet = servletContext.addServlet("StartBrowserServlet", StartBrowserServlet.class);
		startBrowserServlet.setLoadOnStartup(2);
	}
	
}
