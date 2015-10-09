package net.sf.sitemonitoring;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import org.hsqldb.jdbc.JDBCDataSource;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile({ "dev", "standalone" })
@SpringBootApplication
public class Main {

	private static org.hsqldb.server.Server hsql;

	public static void main(String[] args) throws SQLException, IOException {
		if (args.length != 0 && args[0].trim().startsWith("--reset-admin-credentials")) {
			resetCredentialsCommand();
			return;
		}
		clearTempDirectory();
		start(args);
	}

	private static void startHsqlServer() {
		hsql = new org.hsqldb.server.Server();
		hsql.setDatabasePath(0, "file:monit/data");
		hsql.setDatabaseName(0, "data");
		hsql.setSilent(true);
		hsql.setPort(Integer.parseInt(System.getProperty("dbport")));
		hsql.setNoSystemExit(true);
		hsql.start();
	}

	/**
	 * Start Spring application.
	 * 
	 * @param args
	 *            Arguments
	 */
	public static void start(String[] args) {
		log.info("Starting HSQL database in server mode");
		System.out.println("*** STARTING DATABASE ***");
		// first start server
		startHsqlServer();
		try {
			// then stop the server in order to move log to script
			hsql.stop();
			while (true) {
				hsql.checkRunning(false);
				Thread.sleep(200);
			}
		} catch (Exception ex) {
			// server stopped, now start server again
			startHsqlServer();
		}
		System.out.println("*** DATABASE STARTED ***");
		log.info("Starting Spring Boot application");
		new SpringApplicationBuilder(Main.class).headless(false).run(args);
	}

	/**
	 * Creates empty "sitemonitoring-temp" directory. If directory already
	 * exists, it will recursively remote it's contents.
	 * 
	 * @throws IOException
	 */
	private static void clearTempDirectory() throws IOException {
		Path directory = Paths.get("sitemonitoring-temp");
		if (Files.exists(directory)) {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});
		}
		Files.createDirectory(directory);
	}

	/**
	 * Reset admin credentials to username/password: admin/admin
	 * 
	 * @throws SQLException
	 */
	private static void resetCredentialsCommand() throws SQLException {
		System.out.println("*** RESET ADMIN CREDENTIALS TO admin / admin ***");
		JDBCDataSource dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:hsql://localhost:" + System.getProperty("dbport") + "/data");
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
	}

}
