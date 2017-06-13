package org.concordion.cubano.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads and supplies properties from the config.properties file that are required by the framework.
 * <p>
 * This class can be extended by an AppConfig class to provide application specific properties.
 *
 * @author Andrew Sumner
 */
public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private static final String USER_CONFIG_FILE = "user.properties";

    private static Properties properties;
    private static Properties userProperties = null;

    // Environment
    private static String environment = null;

    // Browser 
    private static String browserProvider;
    private static String browserType;
    private static String browserSize;
    private static int browserDefaultTimeout;

    private static String localBrowserExe;
    private static boolean activatePlugins;
    private static String remoteUserName;
    private static String remoteApiKey;

    // Proxy
    private static boolean proxyIsRequired;
    private static String proxyHost;
    private static int proxyPort;
    private static String proxyDomain;
    private static String proxyUsername;
    private static String proxyPassword;


    /** Ensure properties have been loaded before any property is used. */
    static {
        synchronized (Config.class) {
            properties = loadFile(CONFIG_FILE);

            if (new File(USER_CONFIG_FILE).exists()) {
                userProperties = loadFile(USER_CONFIG_FILE);
            }

            loadCommonProperties();
        }
    }

    /**
     * Prevent this class from being constructed.
     */
    protected Config() {
    }

    /**
     * Read properties from file, will ignoring the case of properties.
     *
     * @param filename Name of file to read, expected that it will be located in the projects root folder
     * @return {@link CaselessProperties}
     */
    private static Properties loadFile(final String filename) {
        Properties prop = new CaselessProperties();

        // if (!new File(filename).exists()) {
        // return prop;
        // }

        try (InputStream input = new FileInputStream(filename);) {
            prop.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read properties file.", e);
        }

        return prop;
    }

    private static void loadCommonProperties() {
        // Jenkins might supply value
        environment = System.getProperty("environment", "").toLowerCase();

        if (environment.isEmpty()) {
            environment = getProperty("environment");
        }

        // Browser
        browserProvider = getOptionalProperty("webdriver.browserprovider", "org.concordion.cubano.driver.web.provider.LocalBrowserProvider");
        browserType = System.getProperty("browser");
        if (browserType == null) {
            browserType = getProperty("webdriver.browser");
        }

        browserDefaultTimeout = Integer.parseInt(getProperty("webdriver.defaultTimeout"));
        browserSize = getOptionalProperty("webdriver.browserSize");

        if (useLocalBrowser()) {
            localBrowserExe = getOptionalProperty("webdriver." + browserType + ".exe");
            activatePlugins = Boolean.valueOf(getOptionalProperty("webdriver." + browserType + ".activatePlugins"));
        }

        remoteUserName = getOptionalProperty("remotewebdriver.userName");
        remoteApiKey = getOptionalProperty("remotewebdriver.apiKey");

        // Yandex HtmlElements automatically implement 5 second implicit wait, default to zero so as not to interfere with
        // explicit waits
        System.setProperty("webdriver.timeouts.implicitlywait", getOptionalProperty("webdriver.timeouts.implicitlywait", "0"));

        // Proxy
        proxyIsRequired = Boolean.parseBoolean(getProperty("proxy.required"));
        proxyHost = getProperty("proxy.host");
        proxyPort = Integer.parseInt(getProperty("proxy.port"));

        proxyDomain = getOptionalProperty("proxy.domain");
        proxyUsername = getOptionalProperty("proxy.username");
        proxyPassword = getOptionalProperty("proxy.password");
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @return Property value if found, throws exception if not found
     */
    protected static String getProperty(String key) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
            throw new RuntimeException(String.format("Unable to find property %s", key));
        }

        return value;
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @return Property value if found, empty string if not found
     */
    protected static String getOptionalProperty(String key) {
        return retrieveProperty(key);
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key          Id of the property to look up
     * @param defaultValue value to use if property is not found
     * @return Property value if found, defaultValue if not found
     */
    protected static String getOptionalProperty(String key, String defaultValue) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        return value;
    }

    private static String retrieveProperty(String key) {
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

    private static String retrievePropertyFrom(Properties properties, String key) {
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

    /**
     * Release properties lists held by this class.
     */
    protected static void releaseProperties() {
        properties = null;
        userProperties = null;
    }

    /**
     * @return Configured environment.
     */
    public static String getEnvironment() {
        return environment;
    }

    // Browser
    private static boolean useLocalBrowser() {
        return !browserType.contains(" ");
    }

    public static String getBrowser() {
        return browserType;
    }

    public static String getBrowserProvider() {
        return browserProvider;
    }

    /**
     * Useful if local browser is not available on path.
     *
     * @return Path to browser executable
     */
    public static String getBrowserExe() {
        if (localBrowserExe != null && !localBrowserExe.isEmpty()) {
            return localBrowserExe.replace("%USERPROFILE%", System.getProperty("USERPROFILE", ""));
        }

        return "";
    }

    /**
     * Activate developer plugins - FireFox only browser supported currently and will add FireBug and FirePath.
     *
     * @return true or false
     */
    public static boolean activatePlugins() {
        return activatePlugins;
    }

    /**
     * Size to set browser window - will default to maximised.
     *
     * @return Size in wxh format
     */
    public static String getBrowserSize() {
        return browserSize;
    }

    /**
     * Default timeout in seconds.
     *
     * @return timeout
     */
    public static int getDefaultTimeout() {
        return browserDefaultTimeout;
    }

    /**
     * Username for remote selenium grid service.
     *
     * @return Username
     */
    public static String getRemoteUserName() {
        return remoteUserName;
    }

    /**
     * Api Key to access a remote selenium grid service.
     *
     * @return Api Key
     */
    public static String getRemoteApiKey() {
        return remoteApiKey;
    }

    /**
     * Proxy should be setup or not.
     *
     * @return true or false
     */
    public static boolean isProxyRequired() {
        return proxyIsRequired;
    }

    /**
     * Proxy host name.
     *
     * @return host
     */
    public static String getProxyHost() {
        return proxyHost;
    }

    /**
     * Proxy port number.
     *
     * @return port
     */
    public static int getProxyPort() {
        return proxyPort;
    }

    /**
     * Proxy user's domain.
     *
     * @return domain
     */
    public static String getProxyDomain() {
        if (proxyDomain == null) {
            throw new RuntimeException("proxy.domain entry must exist in the user.properties file in the root folder");
        }

        return proxyDomain;
    }

    /**
     * Proxy username.
     *
     * @return username
     */
    public static String getProxyUser() {
        if (proxyUsername == null) {
            throw new RuntimeException("proxy.username entry must exist in the user.properties file in the root folder");
        }

        return proxyUsername;
    }

    /**
     * Proxy user.
     *
     * @return user
     */
    public static String getProxyPassword() {
        if (proxyPassword == null) {
            throw new RuntimeException("proxy.proxypassword entry must exist in the user.properties file in the root folder");
        }

        return proxyPassword;
    }

    /**
     * @return Proxy bypass (noproxy) addresses, eg: "localhost, 127.0.0.1".
     */
    public static String getNoProxyList() {
        return "localhost, 127.0.0.1";
    }

}