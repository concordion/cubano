package org.concordion.cubano.driver.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

import org.junit.Test;

public class HttpEasyReaderTests {
	private static final String URL = "https://jsonplaceholder.typicode.com";

	@Test
	public void httpEasyRequest() throws HttpResponseException, IOException {
		HttpEasy easy = HttpEasy.request().baseURI(URL).path("/posts/1");

		HttpEasy.withDefaults()
			.allowAllHosts()
			.trustAllCertificates();
//			.baseUrl(AppConfig.getInstance().getBaseUrl());

//	if (Config.getInstance().getProxyConfig().isProxyRequired()) {
		HttpEasy.withDefaults()
				.proxy(new Proxy(Proxy.Type.HTTP,
						new InetSocketAddress("ProxyHost", 8080)))
				//.proxyAuth(Config.getInstance().getProxyConfig().getProxyUsername(), Config.getInstance().getProxyConfig().getProxyPassword())
				.bypassProxyForLocalAddresses(true);
//	}

	
		JsonReader response = easy
				.withLogWriter(new TestLogWriter())
				.logRequestDetails()
				.get()
				.getJsonReader();
	}

}
