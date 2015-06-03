package main;

import java.util.Timer;
import java.util.TimerTask;

import org.boris.winrun4j.Service;
import org.boris.winrun4j.ServiceException;

public class WindowsServiceMain implements Service {

	public static void main(String[] args) throws Exception {
		// fix for WinRun4J
		// http://stackoverflow.com/questions/14780911/winrun4j-cant-load-library-jars
		Thread.currentThread().setContextClassLoader(WindowsServiceMain.class.getClassLoader());
		Main.main(args);
	}

	@Override
	public int serviceMain(String[] args) throws ServiceException {
		try {
			main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int serviceRequest(int control) throws ServiceException {
		try {
			switch (control) {
			case SERVICE_CONTROL_SHUTDOWN:
			case SERVICE_CONTROL_STOP:
				Main.jetty.stop();
				Main.hsql.stop();
				// after 5 seconds terminate the application
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						System.exit(0);
					}
				}, 5000);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
