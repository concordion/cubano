package org.concordion.cubano.driver.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class HttpEasyReaderTests {

    @Test
    public void httpEasyRequest() throws HttpResponseException, IOException {
        // As there are no Logback configuration files in this project we're not getting the TRACE level logs.
        // This has been done solely to view the ProxyVole log output
        Logger logger = (Logger) LoggerFactory.getILoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ALL);
        
        HttpEasy.withDefaults().proxyConfiguration(ProxyConfiguration.AUTOMATIC);

        JsonReader response = HttpEasy.request()
                .baseUrl("http://httpbin.org")
                .header("hellow", "world")
                .path("get")
                .queryParam("name", "fred")
                .logRequestDetails()
                .get()
                .getJsonReader();

        assertThat(response.getAsString("url"), is("http://httpbin.org/get?name=fred"));

    }

}
