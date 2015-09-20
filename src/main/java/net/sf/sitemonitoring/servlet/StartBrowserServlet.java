package net.sf.sitemonitoring.servlet;

import java.awt.Desktop;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Slf4j
public class StartBrowserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		Environment environment = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getEnvironment();
		boolean standaloneProfileActive = environment.acceptsProfiles("standalone");
		System.out.println("standalone profile active: " + standaloneProfileActive);
		if (!standaloneProfileActive) {
			return;
		}
		try {
			System.out.println("desktop supported: " + Desktop.isDesktopSupported());
			if (Desktop.isDesktopSupported()) {
				System.out.println("*** START DEFAULT BROWSER ***");
				Desktop.getDesktop().browse(new URI("http://localhost:" + environment.getProperty("server.port")));
			}
		} catch (Throwable ex) {
			log.error("Couldn't start default browser", ex);
		}
	}
}
