package org.concordion.cubano.utils;

import java.util.Properties;

public class ConfigMock extends Config {

	public ConfigMock(Properties properties) {
		super(properties);
	}
	
	@Override
	protected void loadProperties() {
		
	}
}
