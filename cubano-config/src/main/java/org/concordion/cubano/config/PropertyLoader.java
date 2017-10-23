package org.concordion.cubano.config;

import java.util.Map;

/**
 * Loads and lists properties.
 */
public interface PropertyLoader {

    /**
     * Get the property for the current environment, if that is not found it will look for "{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @return Property value if found, defaultValue if not found
     */
    String getProperty(String key);

    /**
     * Get the property for the current environment, if that is not found it will look for "{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @param defaultValue value to use if property is not found
     * @return Property value if found, defaultValue if not found
     */
    String getProperty(String key, String defaultValue);

    /**
     * Get the property for the current environment as a boolean value, if that is not found it will look for "{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @param defaultValue value to use if property is not found
     * @return Property value if found, defaultValue if not found
     */
    boolean getPropertyAsBoolean(String key, String defaultValue);

    /**
     * Get the property for the current environment as a numeric value, if that is not found it will look for "{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @param defaultValue value to use if property is not found
     * @return Property value if found, defaultValue if not found
     */
    int getPropertyAsInteger(String key, String defaultValue);

    /**
     * Returns a map of key value pairs of properties starting with a prefix.
     *
     * @param keyPrefix Search string
     * @return Map
     */
    Map<String, String> getPropertiesStartingWith(String keyPrefix);

    /**
     * Returns a map of key value pairs of properties starting with a prefix.
     *
     * @param keyPrefix Search string
     * @param trimPrefix Remove prefix from key in returned set
     * @return Map
     */
    Map<String, String> getPropertiesStartingWith(String keyPrefix, boolean trimPrefix);
}
