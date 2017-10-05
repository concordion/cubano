package org.concordion.cubano.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Reads and supplies properties from the <code>config.properties</code> file that are required by the framework.
 * <p>
 * An optional <code>user.properties</code> file can set user specific values and allow overriding of defaults.
 * The <code>user.properties</code> file should NEVER be checked into source control.
 * <p>
 * This class can be extended by an <code>AppConfig</code> class to provide application specific properties.
 *
 * TODO Should this be singleton?
 *
 * @author Andrew Sumner
 */
public abstract class Config {
    private Properties properties;
    private Properties userProperties = null;
    private String environment = null;
    
    /**
     * @return Configured environment.
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Prevent this class from being constructed.
     */
    protected Config() {
        this(new DefaultConfigLoader());
    }

    /**
     * Prevent this class from being constructed.
     */
    protected Config(ConfigLoader loader) {
    	this(loader.getProperties(), loader.getUserProperties());
    }

    protected Config(Properties properties) {
        this(properties, null);
    }

    protected Config(Properties properties, Properties userProperties) {
        this.properties = properties;
        this.userProperties = userProperties;
        
        // Try environment variable first
        environment = System.getProperty("environment", "").toLowerCase();

        if (environment.isEmpty()) {
            environment = getProperty("environment");
        }

        loadProperties();
    }

    protected abstract void loadProperties();

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @return Property value if found, throws exception if not found
     */
    public String getProperty(String key) {
    	String value = retrieveProperty(key);

        if (value.isEmpty()) {
            throw new RuntimeException(String.format("Unable to find property %s", key));
        }

        return value;
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key          Id of the property to look up
     * @param defaultValue value to use if property is not found
     * @return Property value if found, defaultValue if not found
     */
    public String getProperty(String key, String defaultValue) {
    	String value = retrieveProperty(key);
	
	    if (value.isEmpty()) {
	    	value = defaultValue == null ? "" : defaultValue;
	    }
	
	    return value;
	}
    
    public boolean getPropertyAsBoolean(String key, String defaultValue) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
        	value = defaultValue == null ? "false" : defaultValue;
        }

        return Boolean.valueOf(value);
    }
    
    public int getPropertyAsInteger(String key, String defaultValue) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
        	value = defaultValue == null ? "0" : defaultValue;
        }

        return Integer.valueOf(value);
    }
        
    /**
     * Returns a map of key value pairs of properties starting with a prefix.
     * 
     * @param keyPrefix Search string
     * @return Map
     */
    public Map<String, String> getPropertiesStartingWith(String keyPrefix) {
    	return getPropertiesStartingWith(keyPrefix, false);
	}
    
    /**
     * Returns a map of key value pairs of properties starting with a prefix.
     * 
     * @param keyPrefix Search string
     * @param trimPrefix Remove prefix from key in returned set
     * @return Map
     */
    public Map<String, String> getPropertiesStartingWith(String keyPrefix, boolean trimPrefix) {
		Map<String, String> result = new HashMap<>();

		searchPropertiesFrom(properties, keyPrefix, trimPrefix, result);
		searchPropertiesFrom(userProperties, keyPrefix, trimPrefix, result);

		return result;    	
    }

	private void searchPropertiesFrom(Properties properties, String keyPrefix, boolean trimPrefix, Map<String, String> result) {
		if (properties == null) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		Enumeration<String> en = (Enumeration<String>) properties.propertyNames();
		while (en.hasMoreElements()) {
			String propName = en.nextElement();
			String propValue = properties.getProperty(propName);

			if (propName.startsWith(keyPrefix)) {
				if (trimPrefix) {
					propName = propName.substring(keyPrefix.length());
				}
				
				result.put(propName, propValue);
			}
		}
	}

    private String retrieveProperty(String key) {
        String value = null;

        // prefix = System.getProperty("user.name").toLowerCase();
        if (userProperties != null) {
            value = retrievePropertyFrom(userProperties, key);
        }

        if (value == null) {
            value = retrievePropertyFrom(properties, key);
        }

        if (value == null) {
            value = "";
        }

        return value;
    }

    private String retrievePropertyFrom(Properties properties, String key) {
        String value = null;

        // Attempt to get setting for environment
        if (environment != null && !environment.isEmpty()) {
            value = properties.getProperty(environment + "." + key);
        }

        // Attempt to get default setting
        if (value == null) {
            value = properties.getProperty(key);
        }

        if (value != null) {
            value = value.trim();
        }

        return value;
    }

}