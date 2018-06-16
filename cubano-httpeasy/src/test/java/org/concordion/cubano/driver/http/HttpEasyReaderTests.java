package org.concordion.cubano.driver.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class HttpEasyReaderTests {

    @Test
    public void httpEasyRequest() throws HttpResponseException, IOException {
        
        HttpEasy.withDefaults().proxyConfiguration(ProxyConfiguration.AUTOMATIC);
        // ProxyConfig proxyConfig = Config.getInstance().getProxyConfig();

        // if (proxyConfig.isProxyRequired()) {
        // HttpEasy.withDefaults()
        // .proxy(new Proxy(Proxy.Type.HTTP,
        // new InetSocketAddress(proxyConfig.getProxyHost(), proxyConfig.getProxyPort())))
        // .proxyAuth(proxyConfig.getProxyUsername(), proxyConfig.getProxyPassword())
        // .bypassProxyForLocalAddresses(true);
        // }

        JsonReader response = HttpEasy.request()
                .baseURI("http://httpbin.org")
                .header("hellow", "world")
                .path("get")
                .queryParam("name", "fred")
                .withLogWriter(new TestLogWriter())
                .logRequestDetails()
                .get()
                .getJsonReader();

        assertThat(response.getAsString("url"), is("http://httpbin.org/get?name=fred"));

    }

}
