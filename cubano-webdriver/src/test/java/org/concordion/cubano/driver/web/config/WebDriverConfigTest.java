package org.concordion.cubano.driver.web.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class WebDriverConfigTest {
    @Rule
    public final RestoreSystemProperties restoreSystemProperties
            = new RestoreSystemProperties();

    @Test
    public void mustSetDefaultProperties() throws Exception {
        Properties properties = mock(Properties.class);
        given(properties.getProperty("environment")).willReturn("UAT");
        given(properties.getProperty("webdriver.browserProvider")).willReturn("firefox");
//        given(properties.getProperty("webdriver.defaultTimeout")).willReturn("30");

        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(config.getEnvironment(), is("UAT"));
        assertThat(config.getBrowserProvider(), is("firefox"));
//        assertThat(config.getDefaultTimeout(), is(30));
        assertThat(config.isProxyRequired(), is(false));
    }

    @Test
    public void canSetProxyProperties() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("proxy.required")).willReturn("true");
        given(properties.getProperty("proxy.host")).willReturn("myproxyhost:9999");
        given(properties.getProperty("proxy.username")).willReturn("mydomain\\me");
        given(properties.getProperty("proxy.password")).willReturn("secret");

        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(config.isProxyRequired(), is(true));
        assertThat(config.getProxyHost(), is("myproxyhost:9999"));
        assertThat(config.getProxyUser(), is("mydomain\\me"));
        assertThat(config.getProxyPassword(), is("secret"));
    }

    @Test
    public void canSetProxyPropertiesEvenIfProxyIsFalse() {
        // TODO Since proxy is used for multiple purposes. Suggest we need to split these different proxies out.
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("proxy.required")).willReturn("false");
        given(properties.getProperty("proxy.host")).willReturn("myproxyhost1");
        given(properties.getProperty("proxy.username")).willReturn("me1");
        given(properties.getProperty("proxy.password")).willReturn("secret1");

        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(config.isProxyRequired(), is(false));
        assertThat(config.getProxyHost(), is("myproxyhost1"));
        assertThat(config.getProxyUser(), is("me1"));
        assertThat(config.getProxyPassword(), is("secret1"));
    }

    @Test
    public void userPropertiesOverrideConfigProperties() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("proxy.required")).willReturn("false");
        given(properties.getProperty("proxy.host")).willReturn("myproxyhost:9999");

        Properties userProperties = mock(Properties.class);
        given(userProperties.getProperty("proxy.required")).willReturn("true");
        given(userProperties.getProperty("proxy.host")).willReturn("myotherproxyhost:6666");

        WebDriverConfig config = new WebDriverConfig(properties, userProperties);

        assertThat(config.isProxyRequired(), is(true));
        assertThat(config.getProxyHost(), is("myotherproxyhost:6666"));
    }

    @Test
    public void environmentPropertiesOverrideDefaultProperties() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("environment")).willReturn("SIT");
//        given(properties.getProperty("SIT.webdriver.defaultTimeout")).willReturn("20");
        given(properties.getProperty("webdriver.defaultTimeout")).willReturn("30");

        WebDriverConfig config = new WebDriverConfig(properties);

//        assertThat(config.getDefaultTimeout(), is(20));
    }

    @Test
    public void systemPropertiesAreLowercase() {
        Properties properties = givenDefaultProperties();
        System.setProperty("environment", "TEST");

        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(config.getEnvironment(), is("test"));
    }

    @Test
    public void systemPropertiesOverrideProperties() {
        Properties properties = givenDefaultProperties();
        System.setProperty("environment", "uat");
        given(properties.getProperty("environment")).willReturn("SIT");
        System.setProperty("browserProvider", "chrome");
        given(properties.getProperty("webdriver.browserProvider")).willReturn("firefox");

        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(config.getEnvironment(), is("uat"));
        assertThat(config.getBrowserProvider(), is("chrome"));
    }

    @Test
    public void localBrowserSettings() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("webdriver.browserSize")).willReturn("1280x1024");
        given(properties.getProperty("firefox.exe")).willReturn("%USERPROFILE%/bin/firefox");
        given(properties.getProperty("firefox.profile")).willReturn("default");
        
        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(config.getBrowserSize(), is("1280x1024"));
        assertThat(config.getBrowserExe("firefox"), is("/bin/firefox"));
        assertThat(config.getProperty("firefox.profile"), is("default"));
    }

    @Test
    public void implicitWaitDefaultsToZero() {
        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is(nullValue()));

        Properties properties = givenDefaultProperties();
        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is("0"));
    }


    @Test
    public void implicitWaitCanBeSet() {
        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is(nullValue()));

        Properties properties = givenDefaultProperties();
        given(properties.getProperty("webdriver.timeouts.implicitlywait")).willReturn("9");

        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is("9"));
    }

    private Properties givenDefaultProperties() {
        Properties properties = mock(Properties.class);
        given(properties.getProperty("environment")).willReturn("UAT");
        given(properties.getProperty("webdriver.browserProvider")).willReturn("firefox");
        given(properties.getProperty("webdriver.defaultTimeout")).willReturn("30");
        return properties;
    }

}