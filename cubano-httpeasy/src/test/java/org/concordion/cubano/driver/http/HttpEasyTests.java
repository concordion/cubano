package org.concordion.cubano.driver.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Test;

public class HttpEasyTests {

    // TODO Either refactor HttpEasy or just give getConnection method package access
    private HttpURLConnection getConnection(HttpEasy request, String url) throws Exception {
        Method method = request.getClass().getDeclaredMethod("getConnection", URL.class);
        method.setAccessible(true);

        return (HttpURLConnection) method.invoke(request, new URL(url));
    }

    @Test
    public void httpUrlReturnsHttpUrlConnectionObject() throws Exception {
        HttpEasy request = HttpEasy.request();

        HttpURLConnection connection = getConnection(request, "http://some.where.unsecure");

        assertThat(connection, is(instanceOf(HttpURLConnection.class)));
        assertThat(connection, not(instanceOf(HttpsURLConnection.class)));
    }

    @Test
    public void httpsUrlReturnsHttpsUrlConnectionObject() throws Exception {
        HttpEasy request = HttpEasy.request();

        HttpURLConnection connection = getConnection(request, "https://some.where.secure");

        assertThat(connection, is(instanceOf(HttpURLConnection.class)));
        assertThat(connection, is(instanceOf(HttpsURLConnection.class)));
    }

    @Test
    public void httpsConnectionsTrustNothingByDefault() throws Exception {
        HttpEasy request = HttpEasy.request();

        HttpURLConnection connection = getConnection(request, "https://some.where.secure");

        HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

        assertThat(httpsConnection.getHostnameVerifier(), is(equalTo(HttpsURLConnection.getDefaultHostnameVerifier())));
        assertThat(httpsConnection.getSSLSocketFactory(), is(equalTo(HttpsURLConnection.getDefaultSSLSocketFactory())));
    }

    @Test
    public void httpsConnectionsTrustEndPointsPerRequest() throws Exception {
        HttpEasy request = HttpEasy.request().trustAllEndPoints(true);

        HttpsURLConnection connection = (HttpsURLConnection) getConnection(request, "https://some.where.secure");

        assertThat(connection.getHostnameVerifier(), not(equalTo(HttpsURLConnection.getDefaultHostnameVerifier())));
        assertThat(connection.getSSLSocketFactory(), not(equalTo(HttpsURLConnection.getDefaultSSLSocketFactory())));
    }

    @Test
    public void httpsConnectionsTrustEndPointsGlobalDefault() throws Exception {
        HttpEasy request = HttpEasy.request();
        HttpEasy.withDefaults().trustAllEndPoints(true);

        HttpsURLConnection connection = (HttpsURLConnection) getConnection(request, "https://some.where.secure");

        assertThat(connection.getHostnameVerifier(), not(equalTo(HttpsURLConnection.getDefaultHostnameVerifier())));
        assertThat(connection.getSSLSocketFactory(), not(equalTo(HttpsURLConnection.getDefaultSSLSocketFactory())));
    }

    @Test
    public void httpsConnectionsTrustEndPointsOverrideGlobalDefault() throws Exception {
        HttpEasy request = HttpEasy.request().trustAllEndPoints(false);
        HttpEasy.withDefaults().trustAllEndPoints(true);

        HttpsURLConnection connection = (HttpsURLConnection) getConnection(request, "https://some.where.secure");

        assertThat(connection.getHostnameVerifier(), is(equalTo(HttpsURLConnection.getDefaultHostnameVerifier())));
        assertThat(connection.getSSLSocketFactory(), is(equalTo(HttpsURLConnection.getDefaultSSLSocketFactory())));
    }

}
