package net.sf.sitemonitoring;

import com.google.common.collect.ImmutableSet;
import com.sun.faces.config.FacesInitializer;
import lombok.extern.slf4j.Slf4j;
import org.atmosphere.cpr.ContainerInitializer;
import org.primefaces.push.PushServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Configuration
@Slf4j
public class WebXmlCommon {

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private Environment environment;

	@PostConstruct
	public void init() throws ServletException {
		boolean dev = false;
		String[] activeProfiles = environment.getActiveProfiles();
		if (Arrays.asList(activeProfiles).contains("dev")) {
			dev = true;
		}
		initialize(servletContext, dev);
	}

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

		Set<Class<?>> clazz = new HashSet<>();
		clazz.add(WebXmlSpringBoot.class);
		facesInitializer.onStartup(clazz, servletContext);
	}

	@Bean
	public ServletRegistrationBean pushServlet() {
		log.info("Constructed pushServlet");
		ServletRegistrationBean<PushServlet> pushServlet = new ServletRegistrationBean<>(new PushServlet(), "/primepush/*");
		pushServlet.addInitParameter("org.atmosphere.annotation.packages", "org.primefaces.push");
		pushServlet.addInitParameter("org.atmosphere.cpr.packages", "WEB-INF/classes/net.sf.sitemonitoring.push,net.sf.sitemonitoring.push");
		pushServlet.setAsyncSupported(true);
		pushServlet.setLoadOnStartup(0);
		pushServlet.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return pushServlet;
	}

	private static class EmbeddedAtmosphereInitializer extends ContainerInitializer implements ServletContextInitializer {

		@Override
		public void onStartup(ServletContext servletContext) throws ServletException {
			log.info("Called EmbeddedAtmosphereInitializer onStartup()");
			onStartup(Collections.emptySet(), servletContext);
		}

	}

	@Bean
	public EmbeddedAtmosphereInitializer atmosphereInitializer() {
		log.info("Constructed atmosphereInitializer");
		return new EmbeddedAtmosphereInitializer();
	}

}
