package net.sf.sitemonitoring.servlet;
import java.awt.Desktop;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebServlet(value = "/undefined", loadOnStartup = 1)
public class StartBrowserServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		if (!"standalone".equals(getServletContext().getInitParameter("spring.profiles.default"))) {
			return;
		}
		try {
			if (Desktop.isDesktopSupported()) {
				System.out.println("*** START DEFAULT BROWSER ***");
				Desktop.getDesktop().browse(new URI("http://localhost:" + getServletContext().getInitParameter("port")));
			}
		} catch (Throwable ex) {
			log.error("Couldn't start default browser", ex);
		}
	}
}
