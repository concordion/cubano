package org.concordion.cubano.utils;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigTest {
    @Test
    public void ctor() throws Exception {
        Properties properties = mock(Properties.class);
        when(properties.getProperty("environment")).thenReturn("UAT");
        when(properties.getProperty("webdriver.browser")).thenReturn("firefox");
        when(properties.getProperty("webdriver.defaultTimeout")).thenReturn("30");

        // TODO @andrew why do we need the other properties when proxy.required = false
        // in fact, why don't we default to false, and only have a proxy if proxy.host is set?
        when(properties.getProperty("proxy.required")).thenReturn("false");
        when(properties.getProperty("proxy.host")).thenReturn("n/a");
        when(properties.getProperty("proxy.port")).thenReturn("0");

        Config config = new Config(properties);

        assertThat(config.getBrowser(), equalTo("firefox"));
        assertThat(config.isProxyRequired(), equalTo(false));
        assertThat(config.getProxyHost(), equalTo("n/a"));
        assertThat(config.getProxyPort(), equalTo(0));
    }
}