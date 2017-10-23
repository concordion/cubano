package org.concordion.cubano.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;

public class ProxyConfigTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private Enumeration<String> empty = Collections.enumeration(Collections.<String> emptyList());

    @Test
    public void mustSetDefaultProperties() throws Exception {
        Properties properties = givenDefaultProperties();
        ProxyConfig proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.isProxyRequired(), is(false));
        assertThat(proxyConfig.getProxyHost(), is(""));
        assertThat(proxyConfig.getProxyPort(), is(0));
        assertThat(proxyConfig.getProxyAddress(), is(""));
        assertThat(proxyConfig.getProxyUsername(), is(""));
        assertThat(proxyConfig.getProxyPassword(), is(""));
        assertThat(proxyConfig.getNonProxyHosts(), is(""));
    }

    @Test
    public void proxySettingsObtainedInOrder() {
        Properties properties = givenDefaultProperties();

        environmentVariables.set("HTTP_PROXY", "http://proxyhost3:9993");

        ProxyConfig proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.getProxyHost(), is("proxyhost3"));

        // Look in System properties second
        System.setProperty("http.proxyHost", "proxyhost2");

        proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.getProxyHost(), is("proxyhost2"));

        // Look in config file first
        given(properties.getProperty("proxy.host")).willReturn("proxyhost1");

        proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.getProxyHost(), is("proxyhost1"));
    }

    @Test
    public void proxyFromConfigFile() {
        Properties properties = givenDefaultProperties();

        given(properties.getProperty("proxy.host")).willReturn("proxyhost4");
        given(properties.getProperty("proxy.port")).willReturn(null);
        given(properties.getProperty("proxy.username")).willReturn("me4");
        given(properties.getProperty("proxy.password")).willReturn("secret4");
        given(properties.getProperty("proxy.nonProxyHosts")).willReturn("no4");

        ProxyConfig proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.getProxyHost(), is("proxyhost4"));
        assertThat(proxyConfig.getProxyPort(), is(80));
        assertThat(proxyConfig.getProxyAddress(), is("proxyhost4"));
        assertThat(proxyConfig.getProxyUsername(), is("me4"));
        assertThat(proxyConfig.getProxyPassword(), is("secret4"));
        assertThat(proxyConfig.getNonProxyHosts(), is("no4"));
    }

    @Test
    public void proxyFromSystemProperties() {
        Properties properties = givenDefaultProperties();

        // 2. System property http.proxyHost
        System.setProperty("http.proxyHost", "proxyhost3");
        // System.setProperty("http.proxyPort", null);
        System.setProperty("http.proxyUser", "domain\\me3");
        System.setProperty("http.proxyPassword", "secret3");
        System.setProperty("http.nonProxyHosts", "no3");

        ProxyConfig proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.getProxyHost(), is("proxyhost3"));
        // This is the default value
        assertThat(proxyConfig.getProxyPort(), is(80));
        assertThat(proxyConfig.getProxyAddress(), is("proxyhost3"));
        assertThat(proxyConfig.getProxyUsername(), is("domain\\me3"));
        assertThat(proxyConfig.getProxyPassword(), is("secret3"));
        assertThat(proxyConfig.getNonProxyHosts(), is("no3"));
    }

    @Test
    public void proxyFromProxyEnvironmentVariable() {
        Properties properties = givenDefaultProperties();

        environmentVariables.set("HTTP_PROXY", "http://me1:secret1@proxyhost1:9991");

        ProxyConfig proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.getProxyHost(), is("proxyhost1"));
        assertThat(proxyConfig.getProxyPort(), is(9991));
        assertThat(proxyConfig.getProxyAddress(), is("proxyhost1:9991"));
        assertThat(proxyConfig.getProxyUsername(), is("me1"));
        assertThat(proxyConfig.getProxyPassword(), is("secret1"));

        // Using default setting
        assertThat(proxyConfig.getNonProxyHosts(), is(nullValue()));
    }

    @Test
    public void proxyFromProxyAndUserEnvironmentAndVariable() {
        Properties properties = givenDefaultProperties();

        environmentVariables.set("HTTP_PROXY", "http://proxyhost2:9992");
        environmentVariables.set("HTTP_PROXY_USER", "me2");
        environmentVariables.set("HTTP_PROXY_PASS", "secret2");
        environmentVariables.set("NO_PROXY", "no2");

        ProxyConfig proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.getProxyHost(), is("proxyhost2"));
        assertThat(proxyConfig.getProxyPort(), is(9992));
        assertThat(proxyConfig.getProxyAddress(), is("proxyhost2:9992"));
        assertThat(proxyConfig.getProxyUsername(), is("me2"));
        assertThat(proxyConfig.getProxyPassword(), is("secret2"));
        assertThat(proxyConfig.getNonProxyHosts(), is("no2"));
    }

    @Test
    public void proxyMustBeConfiguredIfRequired() {
        Properties properties = givenDefaultProperties();

        given(properties.getProperty("proxy.required")).willReturn("true");

        exception.expect(IllegalArgumentException.class);
        @SuppressWarnings("unused")
        ProxyConfig proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));
    }

    @Test
    public void canSetProxyPropertiesEvenIfProxyIsFalse() {
        Properties properties = givenDefaultProperties();

        given(properties.getProperty("proxy.required")).willReturn("false");
        given(properties.getProperty("proxy.host")).willReturn("myproxyhost1");

        ProxyConfig proxyConfig = new ProxyConfig(new DefaultPropertyLoader(properties));

        assertThat(proxyConfig.isProxyRequired(), is(false));
        assertThat(proxyConfig.getProxyHost(), is("myproxyhost1"));
    }

    @SuppressWarnings("unchecked")
    private Properties givenDefaultProperties() {
        Properties properties = mock(Properties.class);

        given(properties.getProperty(eq("environment"), any())).willReturn("UAT");
        given((Enumeration<String>) properties.propertyNames()).willReturn(empty);

        return properties;
    }
}
