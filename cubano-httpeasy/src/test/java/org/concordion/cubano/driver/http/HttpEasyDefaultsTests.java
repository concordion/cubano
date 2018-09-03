package org.concordion.cubano.driver.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;


public class HttpEasyDefaultsTests {
    @Before
    public void resetDefaults() {
        new HttpEasyDefaults()
                .proxy(Proxy.NO_PROXY)
                .nonProxyHosts(HttpEasyDefaults.DEFAULT_PROXY_BYPASS_HOSTS)
                .proxyConfiguration(ProxyConfiguration.MANUAL);
    }

    private HttpEasyDefaults withProxy() {
        return new HttpEasyDefaults()
                .proxy(new Proxy(Type.HTTP, new InetSocketAddress("host", 80)))
                .bypassProxy(true);
    }

    @Test
    public void bypassProxyForLocalHost() throws MalformedURLException {
        withProxy();

        URL url = new URL("http://localhost:8080/some.path");
        
        assertThat(HttpEasyDefaults.getProxy(url), is(bypassingProxy()));
    }

    @Test
    public void bypassProxyForLocalHost_CaseInsensitive() throws MalformedURLException {
        withProxy();

        URL url = new URL("http://LOCALHOST:8080/some.path");

        assertThat(HttpEasyDefaults.getProxy(url), is(bypassingProxy()));
    }

    @Test
    public void bypassProxyForRemoteHost() throws MalformedURLException {
        URL shortUrl = new URL("http://remotehost:8080/some.path");
        URL fullUrl = new URL("http://remotehost.company.com:8080/some.path");
        URL externalUrl = new URL("http://externalhost.company.com:8080/some.path");

        withProxy().nonProxyHosts(HttpEasyDefaults.DEFAULT_PROXY_BYPASS_HOSTS);
        assertThat(HttpEasyDefaults.getProxy(shortUrl), is(usingProxy()));
        assertThat(HttpEasyDefaults.getProxy(fullUrl), is(usingProxy()));
        assertThat(HttpEasyDefaults.getProxy(externalUrl), is(usingProxy()));

        withProxy().nonProxyHosts("localhost,remotehost.company.com");
        assertThat(HttpEasyDefaults.getProxy(shortUrl), is(usingProxy()));
        assertThat(HttpEasyDefaults.getProxy(fullUrl), is(bypassingProxy()));
        assertThat(HttpEasyDefaults.getProxy(externalUrl), is(usingProxy()));

        withProxy().nonProxyHosts("localhost,remote*.company.com");
        assertThat(HttpEasyDefaults.getProxy(shortUrl), is(usingProxy()));
        assertThat(HttpEasyDefaults.getProxy(fullUrl), is(bypassingProxy()));
        assertThat(HttpEasyDefaults.getProxy(externalUrl), is(usingProxy()));

        withProxy().nonProxyHosts("localhost,*.company.com");
        assertThat(HttpEasyDefaults.getProxy(shortUrl), is(usingProxy()));
        assertThat(HttpEasyDefaults.getProxy(fullUrl), is(bypassingProxy()));
        assertThat(HttpEasyDefaults.getProxy(externalUrl), is(bypassingProxy()));

        withProxy().nonProxyHosts("localhost,remotehost");
        assertThat(HttpEasyDefaults.getProxy(shortUrl), is(bypassingProxy()));
        assertThat(HttpEasyDefaults.getProxy(fullUrl), is(usingProxy()));
        assertThat(HttpEasyDefaults.getProxy(externalUrl), is(usingProxy()));

        withProxy().nonProxyHosts("localhost,remote*");
        assertThat(HttpEasyDefaults.getProxy(shortUrl), is(bypassingProxy()));
        assertThat(HttpEasyDefaults.getProxy(fullUrl), is(bypassingProxy()));
        assertThat(HttpEasyDefaults.getProxy(externalUrl), is(usingProxy()));
    }

    @Test
    public void setEndPointAuthorisation() throws Exception {
        // Default User
        HttpEasy.withDefaults().authorization("defaultUser", "defaultPassword");
        HttpEasy request = HttpEasy.request();

        assertThat(request.getAuthorization(), is("defaultUser:defaultPassword"));

        // Request Specific User
        request.authorization("customUser", "customPassword");
        assertThat(request.getAuthorization(), is("customUser:customPassword"));

        // User from URL
        request.baseUrl("http://requestUser:requestPassword@somewhere.com");
        Method method = request.getClass().getDeclaredMethod("getURL");
        method.setAccessible(true);
        Object r = method.invoke(request);

        assertThat(request.getAuthorization(), is("requestUser:requestPassword"));
    }

    @SuppressWarnings("unchecked")
    public static <T> org.hamcrest.Matcher<T> usingProxy() {
        return org.hamcrest.core.IsNot.<T> not((T) Proxy.NO_PROXY);
    }

    @SuppressWarnings("unchecked")
    public static <T> org.hamcrest.Matcher<T> bypassingProxy() {
        return org.hamcrest.core.Is.<T> is((T) Proxy.NO_PROXY);
    }
}
