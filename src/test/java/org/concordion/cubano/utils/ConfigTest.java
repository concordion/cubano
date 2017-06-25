package org.concordion.cubano.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ConfigTest {
    @Rule
    public final RestoreSystemProperties restoreSystemProperties
            = new RestoreSystemProperties();

    @Test
    public void ctor() throws Exception {
        Properties properties = mock(Properties.class);
        given(properties.getProperty("environment")).willReturn("UAT");
        given(properties.getProperty("webdriver.browser")).willReturn("firefox");
        given(properties.getProperty("webdriver.defaultTimeout")).willReturn("30");

        Config config = new Config(properties);

        assertThat(config.getEnvironment(), is("UAT"));
        assertThat(config.getBrowser(), is("firefox"));
        assertThat(config.getDefaultTimeout(), is(30));
        assertThat(config.isProxyRequired(), is(false));

    }

    @Test
    public void ctorWithProxy() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("proxy.required")).willReturn("true");
        given(properties.getProperty("proxy.host")).willReturn("myproxyhost");
        given(properties.getProperty("proxy.port")).willReturn("9999");
        given(properties.getProperty("proxy.domain")).willReturn("mydomain");
        given(properties.getProperty("proxy.username")).willReturn("me");
        given(properties.getProperty("proxy.password")).willReturn("secret");

        Config config = new Config(properties);

        assertThat(config.isProxyRequired(), is(true));
        assertThat(config.getProxyHost(), is("myproxyhost"));
        assertThat(config.getProxyPort(), is(9999));
        assertThat(config.getProxyDomain(), is("mydomain"));
        assertThat(config.getProxyUser(), is("me"));
        assertThat(config.getProxyPassword(), is("secret"));
    }

    @Test
    public void userPropertiesOverrideConfigProperties() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("proxy.required")).willReturn("false");
        given(properties.getProperty("proxy.host")).willReturn("myproxyhost");
        given(properties.getProperty("proxy.port")).willReturn("9999");

        Properties userProperties = mock(Properties.class);
        given(userProperties.getProperty("proxy.required")).willReturn("true");
        given(userProperties.getProperty("proxy.host")).willReturn("myotherproxyhost");
        given(userProperties.getProperty("proxy.port")).willReturn("6666");

        Config config = new Config(properties, userProperties);

        assertThat(config.isProxyRequired(), is(true));
        assertThat(config.getProxyHost(), is("myotherproxyhost"));
        assertThat(config.getProxyPort(), is(6666));
    }

    @Test
    public void environmentPropertiesOverrideDefaultProperties() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("environment")).willReturn("SIT");
        given(properties.getProperty("SIT.webdriver.defaultTimeout")).willReturn("20");
        given(properties.getProperty("webdriver.defaultTimeout")).willReturn("30");

        Config config = new Config(properties);

        assertThat(config.getDefaultTimeout(), is(20));
    }

    @Test
    public void systemPropertiesAreLowercase() {
        Properties properties = givenDefaultProperties();
        System.setProperty("environment", "TEST");

        Config config = new Config(properties);

        assertThat(config.getEnvironment(), is("test"));
    }

    @Test
    public void systemPropertiesOverrideProperties() {
        Properties properties = givenDefaultProperties();
        System.setProperty("environment", "uat");
        given(properties.getProperty("environment")).willReturn("SIT");
        System.setProperty("browser", "chrome");
        given(properties.getProperty("webdriver.browser")).willReturn("firefox");

        Config config = new Config(properties);

        assertThat(config.getEnvironment(), is("uat"));
        assertThat(config.getBrowser(), is("chrome"));
    }

    @Test
    public void localBrowserSettings() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("webdriver.browserSize")).willReturn("1280x1024");
        given(properties.getProperty("webdriver.firefox.exe")).willReturn("%USERPROFILE%/bin/firefox");
        given(properties.getProperty("webdriver.firefox.activatePlugins")).willReturn("true");

        Config config = new Config(properties);

        assertThat(config.getBrowserSize(), is("1280x1024"));
        assertThat(config.getBrowserExe(), is("/bin/firefox"));
        assertThat(config.activatePlugins(), is(true));
    }

    @Test
    public void implicitWaitDefaultsToZero() {
        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is(nullValue()));

        Properties properties = givenDefaultProperties();
        Config config = new Config(properties);

        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is("0"));
    }


    @Test
    public void implicitWaitCanBeSet() {
        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is(nullValue()));

        Properties properties = givenDefaultProperties();
        given(properties.getProperty("webdriver.timeouts.implicitlywait")).willReturn("9");

        Config config = new Config(properties);

        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is("9"));
    }

    private Properties givenDefaultProperties() {
        Properties properties = mock(Properties.class);
        given(properties.getProperty("environment")).willReturn("UAT");
        given(properties.getProperty("webdriver.browser")).willReturn("firefox");
        given(properties.getProperty("webdriver.defaultTimeout")).willReturn("30");
        return properties;
    }

}