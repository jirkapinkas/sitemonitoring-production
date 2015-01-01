
import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.webapp.WebAppContext;

public class Main {

	public static void main(String[] args) throws Exception {
		System.out.println("*** START HSQL SERVER ***");
		org.hsqldb.server.Server hsql = new org.hsqldb.server.Server();
		hsql.setDatabasePath(0, "file:monit/data");
		hsql.setDatabaseName(0, "data");
		hsql.setSilent(true);
		hsql.start();

		System.out.println("*** START JETTY SERVER ***");
		org.eclipse.jetty.server.Server jetty = new org.eclipse.jetty.server.Server(8080);

		// enables annotations
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(jetty);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		ProtectionDomain domain = Main.class.getProtectionDomain();
		URL location = domain.getCodeSource().getLocation();
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		webapp.setWar(location.toExternalForm());
		jetty.setHandler(webapp);

		jetty.start();
		jetty.join();

	}
}
