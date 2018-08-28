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

import org.junit.After;
import org.junit.Test;

public class HttpEasyTests {

    /**
     * Reset to the default value so that tests don't interfere with each other.
     */
    @After
    public void resetStaticState() {
        HttpEasy.withDefaults().trustAllCertificates(false).trustAllHosts(false);
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

        assertHostnameVerifierTrustsNothing(httpsConnection);
        assertSSLSocketFactoryTrustsNothing(httpsConnection);
    }

    @Test
    public void trustAllEndPointsOnRequest_FollowingRequestIsTrusted() throws Exception {
        HttpEasy request = HttpEasy.request().trustAllCertificates(true).trustAllHosts(true);

        HttpsURLConnection connection = (HttpsURLConnection) getConnection(request, "https://some.where.secure");

        assertTrustingHostnameVerifier(connection);
        assertTrustingSSLSocketFactory(connection);
    }

    @Test
    public void trustAllEndPointsOnRequest_RequestsAfterFollowingRequestAreNotTrusted() throws Exception {
        HttpEasy request = HttpEasy.request().trustAllCertificates(true).trustAllHosts(true);
        @SuppressWarnings("unused")
        HttpsURLConnection connection = (HttpsURLConnection) getConnection(request, "https://some.where.secure");

        HttpEasy request2 = HttpEasy.request();
        HttpsURLConnection connection2 = (HttpsURLConnection) getConnection(request2, "https://some.where.else");

        assertHostnameVerifierTrustsNothing(connection2);
        assertSSLSocketFactoryTrustsNothing(connection2);
    }

    @Test
    public void trustAllEndPointsGlobalDefault_FollowingRequestIsTrusted() throws Exception {
        HttpEasy request = HttpEasy.request();
        HttpEasy.withDefaults().trustAllCertificates(true).trustAllHosts(true);

        HttpsURLConnection connection = (HttpsURLConnection) getConnection(request, "https://some.where.secure");

        assertTrustingHostnameVerifier(connection);
        assertTrustingSSLSocketFactory(connection);
    }

    @Test
    public void trustAllEndPointsGlobalDefault_RequestsAfterFollowingRequestAreTrusted() throws Exception {
        HttpEasy.withDefaults().trustAllCertificates(true).trustAllHosts(true);
        HttpEasy request = HttpEasy.request();
        @SuppressWarnings("unused")
        HttpsURLConnection connection = (HttpsURLConnection) getConnection(request, "https://some.where.secure");

        HttpEasy request2 = HttpEasy.request();
        HttpsURLConnection connection2 = (HttpsURLConnection) getConnection(request2, "https://some.where.else");

        assertTrustingHostnameVerifier(connection2);
        assertTrustingSSLSocketFactory(connection2);
    }

    @Test
    public void trustAllEndPointsOnRequest_OverridesGlobalDefault() throws Exception {
        HttpEasy.withDefaults().trustAllCertificates(true).trustAllHosts(true);
        HttpEasy request = HttpEasy.request().trustAllCertificates(false).trustAllHosts(false);

        HttpsURLConnection connection = (HttpsURLConnection) getConnection(request, "https://some.where.secure");

        assertHostnameVerifierTrustsNothing(connection);
        assertSSLSocketFactoryTrustsNothing(connection);
    }

    private void assertTrustingSSLSocketFactory(HttpsURLConnection connection) {
        assertThat(connection.getSSLSocketFactory(), not(equalTo(HttpsURLConnection.getDefaultSSLSocketFactory())));
    }

    private void assertTrustingHostnameVerifier(HttpsURLConnection connection) {
        assertThat(connection.getHostnameVerifier(), not(equalTo(HttpsURLConnection.getDefaultHostnameVerifier())));
    }

    private void assertSSLSocketFactoryTrustsNothing(HttpsURLConnection httpsConnection) {
        assertThat(httpsConnection.getSSLSocketFactory(), is(equalTo(HttpsURLConnection.getDefaultSSLSocketFactory())));
    }

    private void assertHostnameVerifierTrustsNothing(HttpsURLConnection httpsConnection) {
        assertThat(httpsConnection.getHostnameVerifier(), is(equalTo(HttpsURLConnection.getDefaultHostnameVerifier())));
    }


    // getConnection is only required for checking test results, so temporarily change access to method
    private HttpURLConnection getConnection(HttpEasy request, String url) throws Exception {
        Method method = request.getClass().getDeclaredMethod("getConnection", URL.class);
        method.setAccessible(true);

        return (HttpURLConnection) method.invoke(request, new URL(url));
    }
}
