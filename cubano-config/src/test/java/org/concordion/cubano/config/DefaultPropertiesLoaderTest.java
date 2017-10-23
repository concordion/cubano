package org.concordion.cubano.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Test;

public class DefaultPropertiesLoaderTest {

    @Test
    public void userPropertiesOverrideConfigProperties() {
        String defaultProperties = "a.setting=false";
        String userProperties = "a.setting=true";

        Properties properties = new DefaultPropertiesLoader(defaultProperties, "").getProperties();
        assertThat(properties.getProperty("a.setting"), is("false"));

        properties = new DefaultPropertiesLoader(defaultProperties, userProperties).getProperties();
        assertThat(properties.getProperty("a.setting"), is("true"));
    }
}
