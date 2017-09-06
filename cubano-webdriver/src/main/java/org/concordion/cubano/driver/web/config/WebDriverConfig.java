package org.concordion.cubano.driver.web.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.concordion.cubano.utils.CaseSensitiveConfigLoader;
import org.concordion.cubano.utils.ConfigLoader;
import org.concordion.cubano.utils.DefaultConfigLoader;

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
    private Properties properties;
    private Properties userProperties = null;

    // Environment
    private String environment = null;

    // Browser 
    private String browserProvider;
    private String browserType;
    private String browserSize;
    private int browserDefaultTimeout;

    private String localBrowserExe;
    private boolean activatePlugins;
    private String remoteUserName;
    private String remoteApiKey;

    // Proxy
    private boolean proxyIsRequired;
    private String proxyHost;
    private String proxyUsername;
    private String proxyPassword;

    private static class WDCHolder {
        static final WebDriverConfig INSTANCE = new WebDriverConfig();
    }

    public static WebDriverConfig getInstance() {
        return WDCHolder.INSTANCE;
    }

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

    private void loadCommonProperties() {
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
        proxyIsRequired = Boolean.parseBoolean(getOptionalProperty("proxy.required"));

        proxyHost = getProperty("proxy.host", proxyIsRequired);
        proxyUsername = getOptionalProperty("proxy.username");
        proxyPassword = getOptionalProperty("proxy.password");

		// Make all WebDriverManager properties system properties
		Map<String, String> result = getPropertiesStartingWith("wdm.");

		for (String key : result.keySet()) {
			System.setProperty(key, result.get(key));
		}
    }

	protected Map<String, String> getPropertiesStartingWith(String keyPrefix) {
		Map<String, String> result = new HashMap<>();

		searchPropertiesFrom(new CaseSensitiveConfigLoader().getProperties(), keyPrefix, result);
		searchPropertiesFrom(new CaseSensitiveConfigLoader().getUserProperties(), keyPrefix, result);

		return result;
	}

	private void searchPropertiesFrom(Properties properties, String keyPrefix, Map<String, String> result) {
		@SuppressWarnings("unchecked")
		Enumeration<String> en = (Enumeration<String>) properties.propertyNames();
		while (en.hasMoreElements()) {
			String propName = en.nextElement();
			String propValue = properties.getProperty(propName);

			if (propName.startsWith(keyPrefix)) {
				result.put(propName, propValue);
			}
		}
	}

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @param isRequired true if the property is mandatory, throws RuntimeException if true and property not present
     * @return Property value if found, throws exception if not found
     */
    protected String getProperty(String key, boolean isRequired) {
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
    protected String getProperty(String key) {
        return getProperty(key, false);
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @return Property value if found, empty string if not found
     */
    protected String getOptionalProperty(String key) {
        return retrieveProperty(key);
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key          Id of the property to look up
     * @param defaultValue value to use if property is not found
     * @return Property value if found, defaultValue if not found
     */
    protected String getOptionalProperty(String key, String defaultValue) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        return value;
    }

    private String retrieveProperty(String key) {
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

    private String retrievePropertyFrom(Properties properties, String key) {
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
    public String getEnvironment() {
        return environment;
    }

    // Browser
    private boolean useLocalBrowser() {
        return !browserType.contains(" ");
    }

    public String getBrowser() {
        return browserType;
    }

    public String getBrowserProvider() {
        return browserProvider;
    }

    /**
     * Useful if local browser is not available on path.
     *
     * @return Path to browser executable
     */
    public String getBrowserExe() {
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
    public boolean activatePlugins() {
        return shouldActivatePlugins();
    }

    /**
     * Activate developer plugins - FireFox only browser supported currently and will add FireBug and FirePath.
     *
     * @return true or false
     */
    public boolean shouldActivatePlugins() {
        return activatePlugins;
    }

    /**
     * Size to set browser window - will default to maximised.
     *
     * @return Size in wxh format
     */
    public String getBrowserSize() {
        return browserSize;
    }

    /**
     * Default timeout in seconds.
     *
     * @return timeout
     */
    public int getDefaultTimeout() {
        return browserDefaultTimeout;
    }

    /**
     * Username for remote selenium grid service.
     *
     * @return Username
     */
    public String getRemoteUserName() {
        return remoteUserName;
    }

    /**
     * Api Key to access a remote selenium grid service.
     *
     * @return Api Key
     */
    public String getRemoteApiKey() {
        return remoteApiKey;
    }

    /**
     * Proxy should be setup or not.
     *
     * @return true or false
     */
    public boolean isProxyRequired() {
        return proxyIsRequired;
    }

    /**
     * Proxy host name.
     *
     * @return host
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Proxy username.
     *
     * @return username
     */
    public String getProxyUser() {
// TODO: Not sure that proxy user should fail if not provided
//        if (proxyUsername == null) {
//            throw new RuntimeException("proxy.username entry must exist in the user.properties file in the root folder");
//        }

        return proxyUsername;
    }

    /**
     * Proxy user.
     *
     * @return user
     */
    public String getProxyPassword() {
//  TODO: Not sure that proxy password should fail if not provided
//        if (proxyPassword == null) {
//            throw new RuntimeException("proxy.proxypassword entry must exist in the user.properties file in the root folder");
//        }

        return proxyPassword;
    }

    /**
     * @return Proxy bypass (noproxy) addresses, eg: "localhost, 127.0.0.1".
     */
    public String getNoProxyList() {
        return "localhost, 127.0.0.1";
    }

}