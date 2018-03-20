package org.concordion.cubano.config;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads configuration from config.properties and user.properties located in projects root folder.
 * 
 * It also ensures that any single slash character '\' does not require a double slash. This means that
 * using \ to wrap long property values across multiple lines won't work.
 */
public class DefaultPropertiesLoader implements PropertiesLoader {
    private static final String CONFIG_FILE = "config.properties";
    private static final String USER_CONFIG_FILE = "user.properties";

    private final Properties properties;

    private static class DPHolder {
        static final DefaultPropertiesLoader INSTANCE = new DefaultPropertiesLoader();
    }

    public static DefaultPropertiesLoader getInstance() {
        return DPHolder.INSTANCE;
    }

    /**
     * Allow injection of properties for testing purposes.
     *
     * @param properties Default properties
     * @param userProperties Custom user properties
     */
    protected DefaultPropertiesLoader(String properties, String userProperties) {
        this.properties = new CaselessProperties();

        loadFromString(properties);
        loadFromString(userProperties);
    }

    /** Ensure properties have been loaded before any property is used. */
    private DefaultPropertiesLoader() {
        synchronized (DefaultPropertiesLoader.class) {
            properties = new CaselessProperties();

            loadFile(CONFIG_FILE);

            if (new File(USER_CONFIG_FILE).exists()) {
                loadFile(USER_CONFIG_FILE);
            }
        }
    }

    /**
     * Read properties from file, will ignoring the case of properties.
     * Will override any previously applied settings.
     *
     * @param filename Name of file to read, expected that it will be located in the projects root folder
     * @return {@link CaselessProperties}
     */
    private void loadFile(final String filename) {
        try {
            loadFromString(new String(Files.readAllBytes(Paths.get(filename))));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read properties file " + filename, e);
        }
    }

    private void loadFromString(final String content) {
        try {
            // By default property files treat \ as an escape character so this breaks standard behaviour
            properties.load(new StringReader(content.replace("\\", "\\\\")));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load properties.", e);
        }
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}