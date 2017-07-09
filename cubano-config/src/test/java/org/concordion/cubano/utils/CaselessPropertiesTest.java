package org.concordion.cubano.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CaselessPropertiesTest {

	@Test
    public void caselessPropertiesIgnoresCaseOfKey() {
    	CaselessProperties properties = new CaselessProperties();
    	properties.put("ENvironMENT", "UaT");

        assertThat(properties.getProperty("ENVIRONMENT"), is("UaT"));        
        assertThat(properties.getProperty("environment"), is("UaT"));
    }
}
