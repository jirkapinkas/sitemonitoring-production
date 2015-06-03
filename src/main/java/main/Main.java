package main;
import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.eclipse.jetty.webapp.WebAppContext;
import org.hsqldb.jdbc.JDBCDataSource;

public class Main {
	
	protected static org.hsqldb.server.Server hsql;
	
	protected static org.eclipse.jetty.server.Server jetty;

	public static void main(String[] args) throws Exception {
		int httpPort = 8081;
		int hsqlPort = 9001;

		for (String argument : args) {
			if (argument.startsWith("--port=")) {
				String portString = argument.replace("--port=", "");
				try {
					httpPort = Integer.parseInt(portString);
				} catch (NumberFormatException ex) {
					System.out.println("invalid port specified: " + portString);
				}
			} else if(argument.startsWith("--dbport=")) {
				String portString = argument.replace("--dbport=", "");
				try {
					hsqlPort = Integer.parseInt(portString);
				} catch (NumberFormatException ex) {
					System.out.println("invalid db port specified: " + portString);
				}
			}
		}

		if (args.length != 0 && args[0].startsWith("--reset-admin-credentials")) {
			System.out.println("*** RESET ADMIN CREDENTIALS TO admin / admin ***");
			JDBCDataSource dataSource = new JDBCDataSource();
			dataSource.setUrl("jdbc:hsqldb:hsql://localhost:" + hsqlPort + "/data");
			dataSource.setUser("sa");
			dataSource.setPassword("");
			Connection connection = dataSource.getConnection();
			// admin ~
			// $2a$10$UHdpe.t2Xr3npu1AcDygO.FkiK5Ki4SmUU8oW.gD8liApMG4yDqw6
			PreparedStatement preparedStatement = connection
					.prepareStatement("update monit_configuration set admin_username = 'admin', admin_password = '$2a$10$UHdpe.t2Xr3npu1AcDygO.FkiK5Ki4SmUU8oW.gD8liApMG4yDqw6'");
			preparedStatement.executeUpdate();
			preparedStatement.close();
			connection.close();
			return;
		}

		System.out.println("*** START HSQL SERVER ***");
		hsql = new org.hsqldb.server.Server();
		hsql.setDatabasePath(0, "file:monit/data");
		hsql.setDatabaseName(0, "data");
		hsql.setSilent(true);
		hsql.setPort(hsqlPort);
		hsql.start();

		System.out.println("*** START JETTY SERVER ***");
		System.out.println("*** USING PORT " + httpPort + " ***");
		jetty = new org.eclipse.jetty.server.Server(httpPort);

		// enables annotations
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(jetty);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		ProtectionDomain domain = Main.class.getProtectionDomain();
		URL location = domain.getCodeSource().getLocation();
		WebAppContext webapp = new WebAppContext();
		// classpath scanning optimization ... if something isn't working on
		// standalone deployment but elsewhere it works, this is the culprit
		webapp.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", ".*spring-security-web.*\\.jar|.*primefaces.*\\.jar|.*atmosphere.*\\.jar$");
		webapp.setContextPath("/");
		webapp.setWar(location.toExternalForm());
		webapp.setTempDirectory(new File("temp"));

		// won't delete temp directory
		webapp.setPersistTempDirectory(true);

		jetty.setHandler(webapp);

		webapp.setInitParameter("httpPort", String.valueOf(httpPort));
		webapp.setInitParameter("hsqlPort", String.valueOf(hsqlPort));

		jetty.start();
		jetty.join();

	}
}
