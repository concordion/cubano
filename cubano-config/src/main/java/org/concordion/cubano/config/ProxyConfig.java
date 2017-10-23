package org.concordion.cubano.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

/**
 * Configuration for proxies - used by multiple packages within Cubano.
 */
public class ProxyConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfig.class);

    private boolean proxyIsRequired;
    private String proxyHost;
    private int proxyPort = 0;
    private String proxyUsername = "";
    private String proxyPassword = "";
    private String nonProxyHosts;

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
    public String getProxyUsername() {
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
     *
     * @return The hosts that should be accessed without going through the proxy
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    private void setProxyFromConfigFile(PropertyLoader propertyLoader) {
        proxyHost = propertyLoader.getProperty("proxy.host", "");

        if (proxyHost.isEmpty()) {
            return;
        }

        LOGGER.debug("Loading Proxy settings from configuration file(s)");

        proxyPort = propertyLoader.getPropertyAsInteger("proxy.port", "80");
        proxyUsername = propertyLoader.getProperty("proxy.username", "");
        proxyPassword = propertyLoader.getProperty("proxy.password", "");
        nonProxyHosts = propertyLoader.getProperty("proxy.nonProxyHosts", "");
    }

    private void setProxyFromSystemProperties() {
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

    private void setProxyFromEnvironmentVariables() {
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

    void loadProxyProperties(PropertyLoader propertyLoader) {
        proxyIsRequired = propertyLoader.getPropertyAsBoolean("proxy.required", null);

        setProxyFromConfigFile(propertyLoader);
        setProxyFromSystemProperties();
        setProxyFromEnvironmentVariables();

        if (proxyIsRequired && proxyHost.isEmpty()) {
            propertyLoader.getProperty("proxy.host");
        }
    }
}
