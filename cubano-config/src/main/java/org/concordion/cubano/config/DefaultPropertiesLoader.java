package org.concordion.cubano.config;

import java.io.File;
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
     * Read properties from file, will override and previously applied settings.
     *
     * @param filename Name of file to read, expected that it will be located in the projects root folder
     * @return {@link CaselessProperties}
     */
    private void loadFile(final String filename) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));

            // By default property files treat \ as an escape character so this breaks that behaviour
            properties.load(new StringReader(content.replace("\\", "\\\\")));
        } catch (Exception e) {
            throw new RuntimeException("Unable to read properties file.", e);
        }
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
