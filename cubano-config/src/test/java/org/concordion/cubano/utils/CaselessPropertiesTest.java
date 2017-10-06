package org.concordion.cubano.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
		
		Enumeration<String> en = (Enumeration<String>) properties.propertyNames();
		String propName = en.nextElement();
			
		assertThat(propName, is("a.SETTING"));		
	}	
	
}
