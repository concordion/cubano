package org.concordion.cubano.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class DefaultConfigLoader implements ConfigLoader {
    private final String CONFIG_FILE = "config.properties";
    private final String USER_CONFIG_FILE = "user.properties";

    private final Properties properties;
    private Properties userProperties;

    /** Ensure properties have been loaded before any property is used. */
    public DefaultConfigLoader() {
        synchronized (DefaultConfigLoader.class) {
            properties = loadFile(CONFIG_FILE);

            if (new File(USER_CONFIG_FILE).exists()) {
                userProperties = loadFile(USER_CONFIG_FILE);
            }
        }
    }

    /**
     * Read properties from file, will ignoring the case of properties.
     *
     * @param filename Name of file to read, expected that it will be located in the projects root folder
     * @return {@link CaselessProperties}
     */
    private Properties loadFile(final String filename) {
        Properties prop = new CaselessProperties();

        try (InputStream input = new FileInputStream(filename);) {
            prop.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read properties file.", e);
        }

        return prop;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Properties getUserProperties() {
        return userProperties;
    }
}
