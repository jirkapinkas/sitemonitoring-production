package net.sf.sitemonitoring.service.check.util;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public final class JettyServerUtil {
	
	private JettyServerUtil() {
	}

	public static Server start() throws Exception {
		System.out.println("*** STARTED TEST JETTY SERVER ***");
		Server jettyServer = new Server(8081);

		// enables annotations
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(jettyServer);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/");
		webAppContext.setWar("src/test/resources/test-app.war");
		webAppContext.setTempDirectory(new File("target/jetty-test-app-temp"));
		jettyServer.setHandler(webAppContext);

		jettyServer.start();

		return jettyServer;
	}

	public static void stop(Server jettyServer) throws Exception {
		jettyServer.stop();
		System.out.println("*** STOPPED TEST JETTY SERVER ***");
	}

}
