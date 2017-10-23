package org.concordion.cubano.config;

import java.util.Properties;

/**
 * Configuration loader interface.
 */
public interface PropertiesLoader {

    /**
     * Default properties.
     * 
     * @return Properties
     */
    Properties getProperties();

    /**
     * User specific overrides for the default properties.
     * 
     * @return Properties
     */
    Properties getUserProperties();
}
