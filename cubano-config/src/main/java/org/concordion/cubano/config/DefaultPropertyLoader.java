package org.concordion.cubano.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A PropertyLoader which loads a property, firstly checking the userProperties and then the properties.
 * Within each set of properties, it firstly checks for a key matching <i>environment.key</i> and then for <i>key</i>
 * where <i>environment</i> is set using setEnvironment and <i>key</i> is the key parameter.
 */
public class DefaultPropertyLoader implements PropertyLoader {
    private final Properties properties;
    private String environment;

	/**
	 * Configure the optional userProperties and mandatory properties to be loaded.
	 */
    public DefaultPropertyLoader(Properties properties) {
        this.properties = properties;
    }

	/**
	 * Sets the environment that this property loader will use as a prefix for properties.
	 * @param environment the environment to use as a prefix when looking for a property
	 */
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

    /**
	 * Get the property for the current environment, if that is not found it will look for "{@literal <key>}".
	 *
	 * @param key Id of the property to look up
	 * @return Property value if found, throws exception if not found
	 */
	@Override
	public String getProperty(String key) {
		String value = retrieveProperty(key);

		if (value.isEmpty()) {
			throw new IllegalArgumentException(String.format("Unable to find property %s", key));
		}

		return value;
	}

    /**
	 * Get the property for the current environment, if that is not found it will look for "{@literal <key>}".
	 *
	 * @param key Id of the property to look up
	 * @param defaultValue value to use if property is not found
	 * @return Property value if found, defaultValue if not found
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		String value = retrieveProperty(key);

		if (value.isEmpty()) {
			value = defaultValue == null ? "" : defaultValue;
		}

		return value;
	}

    /**
	 * Get the property for the current environment as a boolean value, if that is not found it will look for "{@literal <key>}".
	 *
	 * @param key Id of the property to look up
	 * @param defaultValue value to use if property is not found
	 * @return Property value if found, defaultValue if not found
	 */
	@Override
	public boolean getPropertyAsBoolean(String key, String defaultValue) {
		String value = retrieveProperty(key);

		if (value.isEmpty()) {
			value = defaultValue == null ? "false" : defaultValue;
		}

		return Boolean.valueOf(value);
	}

    /**
	 * Get the property for the current environment as a numeric value, if that is not found it will look for "{@literal <key>}".
	 *
	 * @param key Id of the property to look up
	 * @param defaultValue value to use if property is not found
	 * @return Property value if found, defaultValue if not found
	 */
	@Override
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
	@Override
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
	@Override
	public Map<String, String> getPropertiesStartingWith(String keyPrefix, boolean trimPrefix) {
		Map<String, String> result = new HashMap<>();

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

        return result;
	}

    private String retrieveProperty(String key) {
        String value = null;

		// Attempt to get setting for environment
		if (environment != null && !environment.isEmpty()) {
			value = properties.getProperty(environment + "." + key);
		}

		// Attempt to get default setting
		if (value == null) {
			value = properties.getProperty(key);
		}

        // Return empty string rather than null
        if (value == null) {
            value = "";
        } else {
            value = value.trim();
		}

        return value;
	}
}
