package net.sf.sitemonitoring.service.check.util;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.ProxyAuthenticator;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public final class ProxyServerUtil {

	private ProxyServerUtil() {
	}

	public static HttpProxyServer start() {
		System.out.println("*** STARTED TEST PROXY SERVER ***");
		HttpProxyServer proxyServer = DefaultHttpProxyServer.bootstrap().withPort(8082).withProxyAuthenticator(new ProxyAuthenticator() {
			@Override
			public boolean authenticate(String username, String password) {
				return username.equals("test") && password.equals("works");
			}
		}).start();
		return proxyServer;
	}

	public static void stop(HttpProxyServer proxyServer) {
		proxyServer.stop();
		System.out.println("*** STOPPED TEST PROXY SERVER ***");
	}
}
