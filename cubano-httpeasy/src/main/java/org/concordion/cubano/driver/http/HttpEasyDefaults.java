package org.concordion.cubano.driver.http;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.markusbernhardt.proxy.ProxySearch;

/**
 * Allows setting of default properties used by all subsequent HttpEasy requests.
 *
 * @author Andrew Sumner
 */
public class HttpEasyDefaults {
    private static String baseUrl = "";
    private static boolean trustAllEndPoints = false;
    private static List<String> sensitiveParameters = new ArrayList<>();

    // Request authorisation
    private static String authUser = null;
    private static String authPassword = null;

    // Proxy
    private static ProxyConfiguration proxyConfiguration = ProxyConfiguration.MANUAL;
    private static volatile ProxySelector proxySearch = null;
    private static Proxy proxy = Proxy.NO_PROXY;
    private static String proxyUser = null;
    private static String proxyPassword = null;

    // Logging
    private static LogWriter defaultLogWriter = new LoggerLogWriter();
    private static boolean logRequest = true;
    private static boolean logRequestDetails = false;


    /**
     * Create all-trusting certificate and host name verifier per HTTPS request.
     * 
     * @param trustAllEndPoints Set to true to trust all certificates and hosts, the default is false
     * @return A self reference
     */
    public HttpEasyDefaults trustAllEndPoints(boolean trustAllEndPoints) {
        HttpEasyDefaults.trustAllEndPoints = trustAllEndPoints;

        return this;
    }

    /**
     * Add default authorization for any requests made. Will set the auth header for every request.
     *
     * @param username User name, if need NTLM authentication format would be DOMAIN\\user
     * @param password Password
     * @return A self reference
     */
    public HttpEasyDefaults authorization(final String username, final String password) {
        HttpEasyDefaults.authUser = username;
        HttpEasyDefaults.authPassword = password;

        return this;
    }


    /**
     * Set the proxy configuration type.
     * 
     * @param configuration Automatic or Manual (default)
     * @return A self reference
     */
    public HttpEasyDefaults proxyConfiguration(ProxyConfiguration configuration) {
        HttpEasyDefaults.proxyConfiguration = configuration;
        
        if (configuration == ProxyConfiguration.AUTOMATIC && proxySearch == null) {
            synchronized (HttpEasyDefaults.class) {
                if (proxySearch == null) {
                    proxySearch = ProxySearch.getDefaultProxySearch().getProxySelector();
                }
            }
        }
        return this;
    }

    /**
     * Set an entry representing a PROXY connection.
     *
     * @param proxy Sets the default {@link Proxy} to use for all connections
     * @return A self reference
     */
    public HttpEasyDefaults proxy(Proxy proxy) {
        HttpEasyDefaults.proxy = proxy;

        return this;
    }

    /**
     * Set the default username and password for proxy authentication.
     *
     * @param userName Proxy username
     * @param password Proxy password
     * @return A self reference
     */
    public HttpEasyDefaults proxyAuth(String userName, String password) {
        HttpEasyDefaults.proxyUser = userName;
        HttpEasyDefaults.proxyPassword = password;

        return this;
    }

    /**
     * Set the default base url for all HttpEasy requests.
     *
     * @param baseUrl Base URL
     * @return A self reference
     */
    public HttpEasyDefaults baseUrl(String baseUrl) {
        HttpEasyDefaults.baseUrl = baseUrl;

        return this;
    }

    public HttpEasyDefaults sensitiveParameters(String... params) {
        HttpEasyDefaults.sensitiveParameters.addAll(Arrays.asList(params));
        
        return this;
    }

    /**
     * Set the default logger to write to.
     *
     * @param logWriter Log writer implementation
     * @return A self reference
     */
    public HttpEasyDefaults withLogWriter(LogWriter logWriter) {
        HttpEasyDefaults.defaultLogWriter = logWriter;

        return this;
    }

    public HttpEasyDefaults logRequest(boolean logRequest) {
        HttpEasyDefaults.logRequest = logRequest;

        return this;
    }

    public HttpEasyDefaults logRequestDetails() {
        HttpEasyDefaults.logRequestDetails = true;

        return this;
    }

    public HttpEasyDefaults logRequestDetails(boolean logRequestDetails) {
        HttpEasyDefaults.logRequestDetails = logRequestDetails;

        return this;
    }

    public static boolean isTrustAllEndPoints() {
        return trustAllEndPoints;
    }

    public static List<String> getSensitiveParameters() {
        return sensitiveParameters;
    }

    public static String getAuthUser() {
        return HttpEasyDefaults.authUser;
    }

    public static String getAuthPassword() {
        return HttpEasyDefaults.authPassword;
    }

    public static Proxy getProxy(URL url) {
        if (proxyConfiguration == ProxyConfiguration.MANUAL) {
            return proxy;
        }

        Proxy proxy = Proxy.NO_PROXY;

        if (proxySearch != null) {
            // Get list of proxies from default ProxySelector available for given URL
            List<Proxy> proxies = proxySearch.select(getUri(url));

            // Find first proxy for HTTP/S. Any DIRECT proxy in the list returned is only second choice
            if (proxies != null) {
                loop:
                for (Proxy p : proxies) {
                    switch (p.type()) {
                    case HTTP:
                        proxy = p;
                        break loop;
                    case DIRECT:
                        proxy = p;
                        break;
                    default:
                        // ignore other proxy types
                    }
                }
            }
        }

        return proxy;
    }

    public static String getProxyUser() {
        return HttpEasyDefaults.proxyUser;
    }

    public static String getProxyPassword() {
        return HttpEasyDefaults.proxyPassword;
    }

    public static String getBaseUrl() {
        return HttpEasyDefaults.baseUrl;
    }

    public static LogWriter getDefaultLogWriter() {
        return HttpEasyDefaults.defaultLogWriter;
    }

    public static boolean getLogRequest() {
        return logRequest;
    }

    public static boolean getLogRequestDetails() {
        return logRequestDetails;
    }

    private static URI getUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}