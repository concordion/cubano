package org.concordion.cubano.utils;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class DefaultConfigLoader implements ConfigLoader {
    private static final String CONFIG_FILE = "config.properties";
    private static final String USER_CONFIG_FILE = "user.properties";

    private final Properties properties;
    private final Properties userProperties;

    /** Ensure properties have been loaded before any property is used. */
    public DefaultConfigLoader() {
        synchronized (DefaultConfigLoader.class) {
            properties = loadFile(CONFIG_FILE);

            if (new File(USER_CONFIG_FILE).exists()) {
                userProperties = loadFile(USER_CONFIG_FILE);
            } else {
            	userProperties = null;
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

        try {
        	String content = new String(Files.readAllBytes(Paths.get(filename)));
        	
        	// By default property files treat \ as an escape character 
            prop.load(new StringReader(content.replace("\\","\\\\")));
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
