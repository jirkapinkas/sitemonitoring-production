package net.sf.sitemonitoring.service.check.util;

import lombok.extern.slf4j.Slf4j;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.ProxyAuthenticator;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

@Slf4j
public final class ProxyServerUtil {

	private ProxyServerUtil() {
	}

	public static HttpProxyServer start() {
		log.info("*** STARTED TEST PROXY SERVER ***");
		HttpProxyServer proxyServer = DefaultHttpProxyServer.bootstrap().withPort(8089).withProxyAuthenticator(new ProxyAuthenticator() {
			@Override
			public boolean authenticate(String username, String password) {
				return username.equals("test") && password.equals("works");
			}

			@Override
			public String getRealm() {
				return null;
			}
		}).start();
		return proxyServer;
	}

	public static void stop(HttpProxyServer proxyServer) {
		proxyServer.stop();
		log.info("*** STOPPED TEST PROXY SERVER ***");
	}
}
