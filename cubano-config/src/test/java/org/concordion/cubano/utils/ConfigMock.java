package org.concordion.cubano.utils;

import java.util.Properties;

public class ConfigMock extends Config {

	public ConfigMock(Properties properties) {
		super(properties);
	}
	
	public ConfigMock(Properties properties, Properties userProperties) {
		super(properties, userProperties);
	}
	
	@Override
	protected void loadProperties() {
		
	}
}
