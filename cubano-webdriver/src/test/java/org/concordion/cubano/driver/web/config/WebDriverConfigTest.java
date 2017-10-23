package org.concordion.cubano.driver.web.config;

import org.concordion.cubano.config.PropertyLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class WebDriverConfigTest {
    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();
    
    private Enumeration<String> empty = Collections.enumeration(Collections.<String>emptyList());

    @Test
    public void localBrowserSettings() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("webdriver.browser.dimension")).willReturn("1280x1024");
        given(properties.getProperty("webdriver.browser.position")).willReturn("10x10");
        given(properties.getProperty("webdriver.browserSize")).willReturn("1280x1024");
        given(properties.getProperty("firefox.exe")).willReturn("%USERPROFILE%/bin/firefox");
        given(properties.getProperty("firefox.profile")).willReturn("default");
        
        WebDriverConfig config = new WebDriverConfig(properties);
        PropertyLoader propertyLoader = config.getPropertyLoader();
       
        assertThat(config.getBrowserDimension(), is("1280x1024"));
        assertThat(config.getBrowserPosition(), is("10x10"));
        assertThat(propertyLoader.getProperty("firefox.exe"), is("%USERPROFILE%/bin/firefox"));
        assertThat(propertyLoader.getProperty("firefox.profile"), is("default"));
    }

    @Test
    public void implicitWaitDefaultsToZero() {
        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is(nullValue()));

        Properties properties = givenDefaultProperties();
        @SuppressWarnings("unused")
        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is("0"));
    }

    @Test
    public void implicitWaitCanBeSet() {
        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is(nullValue()));

        Properties properties = givenDefaultProperties();
        given(properties.getProperty("webdriver.timeouts.implicitlywait")).willReturn("9");

        @SuppressWarnings("unused")
        WebDriverConfig config = new WebDriverConfig(properties);

        assertThat(System.getProperty("webdriver.timeouts.implicitlywait"), is("9"));
    }

    @SuppressWarnings("unchecked")
    private Properties givenDefaultProperties() {
        Properties properties = mock(Properties.class);
        given(properties.getProperty("environment")).willReturn("UAT");
        given(properties.getProperty("webdriver.browserProvider")).willReturn("firefox");
        given((Enumeration<String>)properties.propertyNames()).willReturn(empty);
        
        return properties;
    }

}