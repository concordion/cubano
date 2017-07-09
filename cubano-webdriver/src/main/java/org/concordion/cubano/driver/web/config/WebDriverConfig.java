package org.concordion.cubano.driver.web.config;

import org.concordion.cubano.utils.ConfigLoader;
import org.concordion.cubano.utils.DefaultConfigLoader;

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
public class WebDriverConfig {

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

    /**
     * Prevent this class from being constructed.
     */
    protected WebDriverConfig() {
        this(new DefaultConfigLoader());
    }

    /**
     * Prevent this class from being constructed.
     */
    protected WebDriverConfig(ConfigLoader loader) {
        this(loader.getProperties(), loader.getUserProperties());
    }

    protected WebDriverConfig(Properties properties) {
        this(properties, null);
    }

    protected WebDriverConfig(Properties properties, Properties userProperties) {
        this.properties = properties;
        this.userProperties = userProperties;
        loadCommonProperties();
    }

    private static void loadCommonProperties() {
        // Jenkins might supply value
        environment = System.getProperty("environment", "").toLowerCase();

        if (environment.isEmpty()) {
            environment = getProperty("environment");
        }

        // Browser
        browserProvider = getOptionalProperty("webdriver.browserprovider", "org.concordion.cubano.driver.web.provider.local.LocalBrowserProvider");
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
        proxyIsRequired = Boolean.parseBoolean(getOptionalProperty("proxy.required"));

        proxyHost = getProperty("proxy.host", proxyIsRequired);
        String proxyPortString = getProperty("proxy.port", proxyIsRequired);
        if (!proxyPortString.isEmpty()) {
            proxyPort = Integer.parseInt(proxyPortString);
        }

        proxyDomain = getOptionalProperty("proxy.domain");
        proxyUsername = getOptionalProperty("proxy.username");
        proxyPassword = getOptionalProperty("proxy.password");
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @param isRequired true if the property is mandatory, throws RuntimeException if true and property not present
     * @return Property value if found, throws exception if not found
     */
    protected static String getProperty(String key, boolean isRequired) {
        String value = retrieveProperty(key);

        if (isRequired && value.isEmpty()) {
            throw new RuntimeException(String.format("Unable to find property %s", key));
        }

        return value;
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @return Property value if found, throws exception if not found
     */
    protected static String getProperty(String key) {
        return getProperty(key, false);
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
     * @deprecated use shouldActivatePlugins
     */
    public static boolean activatePlugins() {
        return shouldActivatePlugins();
    }

    /**
     * Activate developer plugins - FireFox only browser supported currently and will add FireBug and FirePath.
     *
     * @return true or false
     */
    public static boolean shouldActivatePlugins() {
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