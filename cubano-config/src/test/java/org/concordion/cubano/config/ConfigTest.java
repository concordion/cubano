package org.concordion.cubano.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;

public class ConfigTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private Enumeration<String> empty = Collections.enumeration(Collections.<String> emptyList());

    @Test
    public void proxySettingsObtainedInOrder() {
        Properties properties = givenDefaultProperties();

        environmentVariables.set("HTTP_PROXY", "http://proxyhost3:9993");

        // Look in HTTP_PROXY environment variable if not found elsewhere
        Config config = new Config(properties);

        assertThat(config.getProxyConfig().getProxyHost(), is("proxyhost3"));

        // Look in System properties second
        System.setProperty("http.proxyHost", "proxyhost2");

        config = new Config(properties);

        assertThat(config.getProxyConfig().getProxyHost(), is("proxyhost2"));

        // Look in config file first
        given(properties.getProperty("proxy.host")).willReturn("proxyhost1");

        config = new Config(properties);

        assertThat(config.getProxyConfig().getProxyHost(), is("proxyhost1"));
    }

    @Test
    public void proxyFromConfigFile() {
        Properties properties = givenDefaultProperties();

        given(properties.getProperty("proxy.host")).willReturn("proxyhost4");
        given(properties.getProperty("proxy.port")).willReturn(null);
        given(properties.getProperty("proxy.username")).willReturn("me4");
        given(properties.getProperty("proxy.password")).willReturn("secret4");
        given(properties.getProperty("proxy.nonProxyHosts")).willReturn("no4");

        Config config = new Config(properties);

        assertThat(config.getProxyConfig().getProxyHost(), is("proxyhost4"));
        assertThat(config.getProxyConfig().getProxyPort(), is(80));
        assertThat(config.getProxyConfig().getProxyAddress(), is("proxyhost4"));
        assertThat(config.getProxyConfig().getProxyUsername(), is("me4"));
        assertThat(config.getProxyConfig().getProxyPassword(), is("secret4"));
        assertThat(config.getProxyConfig().getNonProxyHosts(), is("no4"));
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

        Config config = new Config(properties);

        assertThat(config.getProxyConfig().getProxyHost(), is("proxyhost3"));
        // This is the default value
        assertThat(config.getProxyConfig().getProxyPort(), is(80));
        assertThat(config.getProxyConfig().getProxyAddress(), is("proxyhost3"));
        assertThat(config.getProxyConfig().getProxyUsername(), is("domain\\me3"));
        assertThat(config.getProxyConfig().getProxyPassword(), is("secret3"));
        assertThat(config.getProxyConfig().getNonProxyHosts(), is("no3"));
    }

    @Test
    public void proxyFromPropxyEnvironmentVariable() {
        Properties properties = givenDefaultProperties();

        environmentVariables.set("HTTP_PROXY", "http://me1:secret1@proxyhost1:9991");

        Config config = new Config(properties);

        assertThat(config.getProxyConfig().getProxyHost(), is("proxyhost1"));
        assertThat(config.getProxyConfig().getProxyPort(), is(9991));
        assertThat(config.getProxyConfig().getProxyAddress(), is("proxyhost1:9991"));
        assertThat(config.getProxyConfig().getProxyUsername(), is("me1"));
        assertThat(config.getProxyConfig().getProxyPassword(), is("secret1"));

        // Using default setting
        assertThat(config.getProxyConfig().getNonProxyHosts(), is(nullValue()));
    }

    @Test
    public void proxyFromProxyAndUserEnvironmentAndVariable() {
        Properties properties = givenDefaultProperties();

        environmentVariables.set("HTTP_PROXY", "http://proxyhost2:9992");
        environmentVariables.set("HTTP_PROXY_USER", "me2");
        environmentVariables.set("HTTP_PROXY_PASS", "secret2");
        environmentVariables.set("NO_PROXY", "no2");

        Config config = new Config(properties);

        assertThat(config.getProxyConfig().getProxyHost(), is("proxyhost2"));
        assertThat(config.getProxyConfig().getProxyPort(), is(9992));
        assertThat(config.getProxyConfig().getProxyAddress(), is("proxyhost2:9992"));
        assertThat(config.getProxyConfig().getProxyUsername(), is("me2"));
        assertThat(config.getProxyConfig().getProxyPassword(), is("secret2"));
        assertThat(config.getProxyConfig().getNonProxyHosts(), is("no2"));
    }

    @Test
    public void proxyMustBeConfiguredIfRequired() {
        Properties properties = givenDefaultProperties();

        given(properties.getProperty("proxy.required")).willReturn("true");

        exception.expect(IllegalArgumentException.class);
        @SuppressWarnings("unused")
        Config config = new Config(properties);
    }


    @Test
    public void canSetProxyPropertiesEvenIfProxyIsFalse() {
        Properties properties = givenDefaultProperties();

        given(properties.getProperty("proxy.required")).willReturn("false");
        given(properties.getProperty("proxy.host")).willReturn("myproxyhost1");

        Config config = new Config(properties);

        assertThat(config.getProxyConfig().isProxyRequired(), is(false));
        assertThat(config.getProxyConfig().getProxyHost(), is("myproxyhost1"));
    }

    @Test
    public void mustSetEnvrionment() throws Exception {
        Properties properties = mock(Properties.class);

        exception.expect(IllegalArgumentException.class);
        @SuppressWarnings("unused")
        Config config = new Config(properties);
    }

    @Test
    public void systemPropertyWillOverrideEnvrionment() throws Exception {
        Properties properties = givenDefaultProperties();

        System.setProperty("environment", "SIT");

        Config config = new Config(properties);

        assertThat(config.getEnvironment(), is("SIT"));
    }

    @Test
    public void mustSetDefaultProperties() throws Exception {
        Properties properties = givenDefaultProperties();

        Config config = new Config(properties);

        assertThat(config.getEnvironment(), is("UAT"));
        assertThat(config.getProxyConfig().isProxyRequired(), is(false));
        assertThat(config.getProxyConfig().getProxyHost(), is(""));
        assertThat(config.getProxyConfig().getProxyPort(), is(0));
        assertThat(config.getProxyConfig().getProxyAddress(), is(""));
        assertThat(config.getProxyConfig().getProxyUsername(), is(""));
        assertThat(config.getProxyConfig().getProxyPassword(), is(""));

        // TODO Fails on my laptop as getting value "local,*.local,169.254/16,*.169.254/16,127.0.0.1" from somewhere - fixed add back in
        // assertThat(config.getNonProxyHosts(), is(""));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void userPropertiesOverrideConfigProperties() {
        Properties properties = givenDefaultProperties();        
        given(properties.getProperty("a.setting")).willReturn("false");

        Properties userProperties = mock(Properties.class);
        given(userProperties.getProperty("a.setting")).willReturn("true");
        given((Enumeration<String>)userProperties.propertyNames()).willReturn(empty);

        Config config = new Config(properties);
        assertThat(config.getPropertyLoader().getProperty("a.setting"), is("false"));

        config = new Config(properties, userProperties);
        assertThat(config.getPropertyLoader().getProperty("a.setting"), is("true"));
    }

    @Test
    public void environmentPropertiesOverrideDefaultProperties() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("a.setting")).willReturn("false");
        given(properties.getProperty("SIT.a.setting")).willReturn("true");

        Config config = new Config(properties);
        assertThat(config.getPropertyLoader().getProperty("a.setting"), is("false"));

        given(properties.getProperty("environment")).willReturn("SIT");        
        config = new Config(properties);
        assertThat(config.getPropertyLoader().getProperty("a.setting"), is("true"));
    }

    @Test
    public void canSearchForPopertiesWithPrefixMatchingCase() throws IOException {
        Properties properties = givenDefaultSearchProperties();

        Config config = new Config(properties);

        Map<String, String> found = config.getPropertyLoader().getPropertiesStartingWith("a.setting.");

        assertThat(found.values().size(), is(1));
        assertThat(found.keySet().iterator().next(), is("a.setting.2"));
    }

    @Test
    public void canSearchForPopertiesAngGetOriginalPropertyCase() throws IOException {
        Properties properties = givenDefaultSearchProperties();

        Config config = new Config(properties);

        Map<String, String> found = config.getPropertyLoader().getPropertiesStartingWith("a.");

        assertThat(found.values().size(), is(2));
        assertThat(found.keySet().iterator().next(), is("a.SETTING.1"));
    }

    @Test
    public void canSearchForPopertiesAngGetKeysWithoutPrefix() throws IOException {
        Properties properties = givenDefaultSearchProperties();

        Config config = new Config(properties);

        Map<String, String> found = config.getPropertyLoader().getPropertiesStartingWith("a.setting.", true);

        assertThat(found.values().size(), is(1));
        assertThat(found.keySet().iterator().next(), is("2"));
    }

    @SuppressWarnings("unchecked")
    private Properties givenDefaultProperties() {
        Properties properties = mock(Properties.class);

        given(properties.getProperty("environment")).willReturn("UAT");
        given((Enumeration<String>) properties.propertyNames()).willReturn(empty);

        return properties;
    }

    private Properties givenDefaultSearchProperties() throws IOException {
        CaselessProperties properties = new CaselessProperties();
        StringBuilder sb = new StringBuilder();

        String newline = System.lineSeparator();
        sb.append("environment=UAT").append(newline);
        sb.append("a.SETTING.1=value1").append(newline);
        sb.append("a.setting.2=value2").append(newline);
        sb.append("b.SETTING.1=value1").append(newline);

        properties.load(new StringReader(sb.toString()));

        return properties;
    }
}
