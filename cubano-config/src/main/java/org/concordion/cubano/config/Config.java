package org.concordion.cubano.config;

import java.util.Properties;

/**
 * Reads and supplies properties from the <code>config.properties</code> file that are required by the framework.
 * <p>
 * An optional <code>user.properties</code> file can set user specific values and allow overriding of defaults.
 * The <code>user.properties</code> file should NEVER be checked into source control.
 *
 * @author Andrew Sumner
 */
public final class Config {

    private final DefaultPropertyLoader propertyLoader;
    private final ProxyConfig proxyConfig = new ProxyConfig();
    private String environment;

    private static class ConfigHolder {
        static final Config INSTANCE = new Config();
    }

    public static Config getInstance() {
        return ConfigHolder.INSTANCE;
    }

	/**
	 * Uses DefaultPropertiesLoader to import the config and user properties files.
	 */
	protected Config() {
		this(DefaultPropertiesLoader.getInstance());
	}

	/**
	 * Uses the supplied PropertiesLoader to import the config and user properties files.
	 *
	 * @param propertiesLoader Configuration loader
	 */
	protected Config(PropertiesLoader propertiesLoader) {
		this(propertiesLoader.getProperties(), propertiesLoader.getUserProperties());
	}

	/**
	 * Allow injection of properties for testing purposes.
	 *
	 * @param properties Default properties
	 */
	protected Config(Properties properties) {
		this(properties, null);
	}

	/**
	 * Load the properties for this configuration.
	 *
	 * @param properties Default properties
	 * @param userProperties User specific overrides
	 */
	protected Config(Properties properties, Properties userProperties) {
        propertyLoader = new DefaultPropertyLoader(properties, userProperties);

        // Try environment variable first
        environment = System.getProperty("environment", "");

        if (environment.isEmpty()) {
            environment = propertyLoader.getProperty("environment");
        }

        propertyLoader.setEnvironment(environment);

        proxyConfig.loadProxyProperties(this, propertyLoader);
	}

    /**
	 * @return Configured environment.
	 */
	public String getEnvironment() {
		return environment;
	}

    /**
     * @return Configuration for proxy.
     */
    public ProxyConfig getProxyConfig() {
	    return proxyConfig;
    }

    /**
     * @return a loader for loading properties across config.properties and user.properties, taking environment into account.
     */
    public PropertyLoader getPropertyLoader() {
        return propertyLoader;
    }
}