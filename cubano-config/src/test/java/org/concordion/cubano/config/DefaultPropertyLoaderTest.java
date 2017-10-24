package org.concordion.cubano.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

public class DefaultPropertyLoaderTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    private Enumeration<String> empty = Collections.enumeration(Collections.<String> emptyList());

    @Test
    public void environmentPropertiesOverrideDefaultProperties() {
        Properties properties = givenDefaultProperties();
        given(properties.getProperty("a.setting")).willReturn("false");
        given(properties.getProperty("SIT.a.setting")).willReturn("true");

        // Check with default env of UAT
        DefaultPropertyLoader loader = new DefaultPropertyLoader(properties);
        assertThat(loader.getProperty("a.setting"), is("false"));

        // Check with env of SIT
        given(properties.getProperty(eq("environment"), any())).willReturn("SIT");
        loader = new DefaultPropertyLoader(properties);

        assertThat(loader.getProperty("a.setting"), is("true"));
    }

    @Test
    public void systemPropertyWillOverrideEnvironment() throws Exception {
        Properties properties = givenDefaultProperties();

        System.setProperty("environment", "SIT");

        DefaultPropertyLoader loader = new DefaultPropertyLoader(properties);

        assertThat(loader.getEnvironment(), is("SIT"));
    }

    @Test
    public void canSearchForPopertiesWithPrefixMatchingCase() throws IOException {
        Properties properties = givenDefaultSearchProperties();

        DefaultPropertyLoader loader = new DefaultPropertyLoader(properties);

        Map<String, String> found = loader.getPropertiesStartingWith("a.setting.");

        assertThat(found.values().size(), is(1));
        assertThat(found.keySet().iterator().next(), is("a.setting.2"));
    }

    @Test
    public void canSearchForPopertiesAngGetOriginalPropertyCase() throws IOException {
        Properties properties = givenDefaultSearchProperties();

        DefaultPropertyLoader loader = new DefaultPropertyLoader(properties);

        Map<String, String> found = loader.getPropertiesStartingWith("a.");

        assertThat(found.values().size(), is(2));
        assertThat(found.keySet().iterator().next(), is("a.SETTING.1"));
    }

    @Test
    public void canSearchForPopertiesAngGetKeysWithoutPrefix() throws IOException {
        Properties properties = givenDefaultSearchProperties();

        DefaultPropertyLoader loader = new DefaultPropertyLoader(properties);

        Map<String, String> found = loader.getPropertiesStartingWith("a.setting.", true);

        assertThat(found.values().size(), is(1));
        assertThat(found.keySet().iterator().next(), is("2"));
    }

    @SuppressWarnings("unchecked")
    private Properties givenDefaultProperties() {
        Properties properties = mock(Properties.class);

        given(properties.getProperty(eq("environment"), any())).willReturn("UAT");
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
