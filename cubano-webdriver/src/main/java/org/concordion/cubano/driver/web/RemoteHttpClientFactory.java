package org.concordion.cubano.driver.web;

import java.net.URL;

import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.remote.internal.ApacheHttpClient;

/**
 * Allows WebDriver to connect to a Selenium grid through a proxy without requiring a tunnel to the provider.
 *
 * @author Andrew Sumner
 */
public class RemoteHttpClientFactory implements org.openqa.selenium.remote.http.HttpClient.Factory {
    private final HttpClientBuilder builder;

    /**
     * Constructor.
     *
     * @param builder Builder
     */
    public RemoteHttpClientFactory(HttpClientBuilder builder) {
        this.builder = builder;
    }

    @Override
    public org.openqa.selenium.remote.http.HttpClient createClient(URL url) {
        return new ApacheHttpClient(builder.build(), url);
    }
}



