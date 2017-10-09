package org.concordion.cubano.utils;

import java.util.Properties;

/**
 * Configuration loader interface.
 */
public interface ConfigLoader {

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
