import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.eclipse.jetty.webapp.WebAppContext;
import org.hsqldb.jdbc.JDBCDataSource;

public class Main {

	public static void main(String[] args) throws Exception {
		if(args.length != 0 && args[0].startsWith("--reset-admin-credentials")) {
			System.out.println("*** RESET ADMIN CREDENTIALS TO admin / admin ***");
			JDBCDataSource dataSource = new JDBCDataSource();
			dataSource.setUrl("jdbc:hsqldb:hsql://localhost/data");
			dataSource.setUser("sa");
			dataSource.setPassword("");
			Connection connection = dataSource.getConnection();
			// admin ~ $2a$10$UHdpe.t2Xr3npu1AcDygO.FkiK5Ki4SmUU8oW.gD8liApMG4yDqw6
			PreparedStatement preparedStatement = connection.prepareStatement("update monit_configuration set admin_username = 'admin', admin_password = '$2a$10$UHdpe.t2Xr3npu1AcDygO.FkiK5Ki4SmUU8oW.gD8liApMG4yDqw6'");
			preparedStatement.executeUpdate();
			preparedStatement.close();
			connection.close();
			return;
		}
		
		System.out.println("*** START HSQL SERVER ***");
		org.hsqldb.server.Server hsql = new org.hsqldb.server.Server();
		hsql.setDatabasePath(0, "file:monit/data");
		hsql.setDatabaseName(0, "data");
		hsql.setSilent(true);
		hsql.start();

		int port = 8081;
		if (args.length != 0 && args[0].startsWith("--port=")) {
			String portString = args[0].replace("--port=", "");
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException ex) {
				System.out.println("invalid port specified: " + portString);
			}
		}

		System.out.println("*** START JETTY SERVER ***");
		System.out.println("*** USING PORT " + port + " ***");
		org.eclipse.jetty.server.Server jetty = new org.eclipse.jetty.server.Server(port);

		// enables annotations
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(jetty);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		ProtectionDomain domain = Main.class.getProtectionDomain();
		URL location = domain.getCodeSource().getLocation();
		WebAppContext webapp = new WebAppContext();
		// classpath scanning optimization ... if something isn't working on standalone deployment but elsewhere it works, this is the culprit
		webapp.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", ".*spring-security-web.*\\.jar|.*primefaces.*\\.jar|.*atmosphere.*\\.jar$");
		webapp.setContextPath("/");
		webapp.setWar(location.toExternalForm());
		webapp.setTempDirectory(new File("temp"));

		// won't delete temp directory
		webapp.setPersistTempDirectory(true);
		
		jetty.setHandler(webapp);
		
		webapp.setInitParameter("port", String.valueOf(port));

		jetty.start();
		jetty.join();

	}
}
