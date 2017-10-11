package org.concordion.cubano.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Enumeration;
import java.util.Properties;

import org.junit.Test;

public class CaselessPropertiesTest {

    @Test
    public void propertyLookupsAreCaseInsensitive() {
        Properties properties = new CaselessProperties();

        properties.put("a.SETTING", "value");

        assertThat(properties.getProperty("a.setting"), is("value"));
    }

    @Test
    public void propertyKeyOriginalCaseIsAvailalbe() {
        Properties properties = new CaselessProperties();

        properties.put("a.SETTING", "value");

        @SuppressWarnings("unchecked")
        Enumeration<String> en = (Enumeration<String>) properties.propertyNames();
        String propName = en.nextElement();

        assertThat(propName, is("a.SETTING"));
    }

}
