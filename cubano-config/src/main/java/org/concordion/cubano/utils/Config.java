package org.concordion.cubano.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class Config {
    public static final String DEFAULT_NON_PROXY_HOSTS = "localhost,127.0.0.1";

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    // TODO Getting multiple instances of Config all with own copy of properties - how can we share this?
    private Properties properties;
    private Properties userProperties = null;

    // Environment
    private String environment = null;

    // Proxy
    private boolean proxyIsRequired;
    private String proxyHost;
    private int proxyPort = 0;
    private String proxyUsername = "";
    private String proxyPassword = "";
    private String nonProxyHosts;

    /**
     * @return Configured environment.
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Prevent this class from being constructed.
     */
    protected Config() {
        this(new DefaultConfigLoader());
    }

    /**
     * Prevent this class from being constructed.
     */
    protected Config(ConfigLoader loader) {
        this(loader.getProperties(), loader.getUserProperties());
    }

    protected Config(Properties properties) {
        this(properties, null);
    }

    protected Config(Properties properties, Properties userProperties) {
        this.properties = properties;
        this.userProperties = userProperties;

        loadSharedProperties();
        loadProperties();
    }

    protected abstract void loadProperties();

    protected void loadSharedProperties() {
        // Try environment variable first
        environment = System.getProperty("environment", "");

        if (environment.isEmpty()) {
            environment = getProperty("environment");
        }

        proxyIsRequired = getPropertyAsBoolean("proxy.required", null);

        setProxyViaConfigFile();
        setProxyViaSystemProperties();
        setProxyViaEnvironmentVariables();

        if (proxyIsRequired && proxyHost.isEmpty()) {
            getProperty("proxy.host");
        }

        nonProxyHosts = nonProxyHosts == null || nonProxyHosts.isEmpty() ? DEFAULT_NON_PROXY_HOSTS : nonProxyHosts;
    }

    private void setProxyViaConfigFile() {
        proxyHost = getProperty("proxy.host", "");

        if (proxyHost.isEmpty()) {
            return;
        }

        LOGGER.debug("Loading Proxy settings from configuration file(s)");

        proxyPort = getPropertyAsInteger("proxy.port", "80");
        proxyUsername = getProperty("proxy.username", "");
        proxyPassword = getProperty("proxy.password", "");
        nonProxyHosts = getProperty("proxy.nonProxyHosts", "");
    }

    private void setProxyViaSystemProperties() {
        if (!proxyHost.isEmpty()) {
            return;
        }

        proxyHost = System.getProperty("http.proxyHost", "");

        if (proxyHost.isEmpty()) {
            return;
        }

        LOGGER.debug("Loading Proxy settings from http.proxy... system properties");

        proxyPort = Integer.valueOf(System.getProperty("http.proxyPort", "80"));
        proxyUsername = System.getProperty("http.proxyUser", "");
        proxyPassword = System.getProperty("http.proxyPassword", "");
        nonProxyHosts = System.getProperty("http.nonProxyHosts", "").replaceAll("\\|", ",");
    }

    private void setProxyViaEnvironmentVariables() {
        if (!proxyHost.isEmpty()) {
            return;
        }

        URL proxyUrl = getProxyUrl();

        if (proxyUrl == null) {
            proxyHost = "";
        } else {
            LOGGER.debug("Loading Proxy settings from HTTP_PROXY environment variable(s)");

            proxyHost = proxyUrl.getHost();
            proxyPort = proxyUrl.getPort();

            String userInfo = proxyUrl.getUserInfo();

            if (userInfo != null) {
                StringTokenizer st = new StringTokenizer(userInfo, ":");

                try {
                    proxyUsername = st.hasMoreTokens() ? URLDecoder.decode(st.nextToken(), StandardCharsets.UTF_8.name()) : null;
                    proxyPassword = st.hasMoreTokens() ? URLDecoder.decode(st.nextToken(), StandardCharsets.UTF_8.name()) : null;
                } catch (UnsupportedEncodingException e) {
                    // TODO log this
                    // do nothing
                }
            }

            if (proxyUsername == null || proxyUsername.isEmpty()) {
                proxyUsername = System.getenv("HTTP_PROXY_USER");
            }

            if (proxyPassword == null || proxyPassword.isEmpty()) {
                proxyPassword = System.getenv("HTTP_PROXY_PASS");
            }

            nonProxyHosts = System.getenv("NO_PROXY");
        }
    }

    private URL getProxyUrl() {
        String proxyInput = System.getenv("HTTP_PROXY");

        try {
            if (proxyInput != null) {
                return new URL(proxyInput.matches("^http[s]?://.*$") ? proxyInput : "http://" + proxyInput);
            }
        } catch (MalformedURLException e) {
            LOGGER.warn("Invalid proxy url {} in HTTP_PROXY environment variable", proxyInput, e);
        }

        return null;
    }

    /**
     * Whether a proxy should be configured for accessing the test application or not, regardless of means of accessing the test
     * application, e.g. web browser or api request.
     *
     * @return true or false
     */
    public boolean isProxyRequired() {
        return proxyIsRequired;
    }

    /**
     * @return The hostname, or address, of the proxy server.
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * @return The port number of the proxy server.
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * @return The hostname and port of the proxy server in the format host:port.
     */
    public String getProxyAddress() {
        if (proxyHost.isEmpty()) {
            return "";
        }

        if (proxyPort == 0 || proxyPort == 80) {
            return proxyHost;
        }

        return proxyHost + ":" + String.valueOf(proxyPort);
    }

    /**
     * @return Username to authenticate connections through the proxy server.
     */
    public String getProxyUser() {
        return proxyUsername;
    }

    /**
     * @return Password to authenticate connections through the proxy server.
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Indicates the hosts that should be accessed without going through the proxy. Typically this defines internal hosts.
     * The value of this property is a list of hosts, separated by the '|' character.
     * In addition the wildcard character '*' can be used for pattern matching.
     * 
     * <p>
     * For example: proxy.nonProxyHosts=*.foo.com,localhost will indicate that every hosts in the foo.com domain and the localhost should be accessed directly
     * even if a proxy server is specified.
     * </p>
     * <p>
     * Defaults to "localhost,127.0.0.1".
     * </p>
     * 
     * @return The hosts that should be accessed without going through the proxy
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @return Property value if found, throws exception if not found
     */
    public String getProperty(String key) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
            throw new IllegalArgumentException(String.format("Unable to find property %s", key));
        }

        return value;
    }

    /**
     * Get the property for the current environment, if that is not found it will look for "default.{@literal <key>}".
     *
     * @param key Id of the property to look up
     * @param defaultValue value to use if property is not found
     * @return Property value if found, defaultValue if not found
     */
    public String getProperty(String key, String defaultValue) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
            value = defaultValue == null ? "" : defaultValue;
        }

        return value;
    }

    public boolean getPropertyAsBoolean(String key, String defaultValue) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
            value = defaultValue == null ? "false" : defaultValue;
        }

        return Boolean.valueOf(value);
    }

    public int getPropertyAsInteger(String key, String defaultValue) {
        String value = retrieveProperty(key);

        if (value.isEmpty()) {
            value = defaultValue == null ? "0" : defaultValue;
        }

        return Integer.valueOf(value);
    }

    /**
     * Returns a map of key value pairs of properties starting with a prefix.
     * 
     * @param keyPrefix Search string
     * @return Map
     */
    public Map<String, String> getPropertiesStartingWith(String keyPrefix) {
        return getPropertiesStartingWith(keyPrefix, false);
    }

    /**
     * Returns a map of key value pairs of properties starting with a prefix.
     * 
     * @param keyPrefix Search string
     * @param trimPrefix Remove prefix from key in returned set
     * @return Map
     */
    public Map<String, String> getPropertiesStartingWith(String keyPrefix, boolean trimPrefix) {
        Map<String, String> result = new HashMap<>();

        searchPropertiesFrom(properties, keyPrefix, trimPrefix, result);
        searchPropertiesFrom(userProperties, keyPrefix, trimPrefix, result);

        return result;
    }

    private void searchPropertiesFrom(Properties properties, String keyPrefix, boolean trimPrefix, Map<String, String> result) {
        if (properties == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Enumeration<String> en = (Enumeration<String>) properties.propertyNames();
        while (en.hasMoreElements()) {
            String propName = en.nextElement();
            String propValue = properties.getProperty(propName);

            if (propName.startsWith(keyPrefix)) {
                if (trimPrefix) {
                    propName = propName.substring(keyPrefix.length());
                }

                result.put(propName, propValue);
            }
        }
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

}