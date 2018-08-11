package org.concordion.cubano.driver.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class HttpEasyReaderTests {

    @Test
    public void httpEasyRequest() throws HttpResponseException, IOException {
        
        HttpEasy.withDefaults().proxyConfiguration(ProxyConfiguration.AUTOMATIC);

        JsonReader response = HttpEasy.request()
                .baseUrl("http://httpbin.org")
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
