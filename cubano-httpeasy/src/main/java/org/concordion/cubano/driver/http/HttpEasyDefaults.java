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
import com.github.markusbernhardt.proxy.util.Logger;

/**
 * Allows setting of default properties used by all subsequent HttpEasy requests.
 *
 * @author Andrew Sumner
 */
public class HttpEasyDefaults {
    public static final String DEFAULT_PROXY_BYPASS_HOSTS = "localhost,127.0.0.1";

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
    private static boolean bypassProxy = false;
    private static List<String> nonProxyHosts = splitHosts(DEFAULT_PROXY_BYPASS_HOSTS);

    // Logging
    private static LogWriter defaultLogWriter = new LoggerLogWriter();
    private static boolean logRequest = true;
    private static boolean logRequestDetails = false;


    /**
     * Skip validation of any SSL certificates and trust all hostnames.
     * Only applies to HTTPS connections.
     *
     * @param trustAllEndPoints Set to true to trust all certificates and hosts, the default is false
     * @return A self reference
     * @see HttpEasy#trustAllEndPoints(boolean) to override this setting per request
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

    private static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Set the proxy configuration type.
     * 
     * @param configuration Automatic or Manual (default)
     * @return A self reference
     */
    public HttpEasyDefaults proxyConfiguration(ProxyConfiguration configuration) {
        HttpEasyDefaults.proxyConfiguration = configuration;
        
        if (configuration == ProxyConfiguration.AUTOMATIC && proxySearch == null && isPresent("com.github.markusbernhardt.proxy.ProxySearch")) {
            synchronized (HttpEasyDefaults.class) {
                if (proxySearch == null) {
                    Logger.setBackend(new ProxyVoleLogger());
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
     * Bypass the proxy, if specified, for addresses in the {@link #nonProxyHosts(String)}.
     * <p>
     * Defaults to false
     * </p>
     * 
     * @param bypassProxy Value
     * @return A self reference
     */
    public HttpEasyDefaults bypassProxy(boolean bypassProxy) {
        HttpEasyDefaults.bypassProxy = bypassProxy;
        return this;
    }

    /**
     * A comma delemeited list of hosts to bypass the proxy for. The items in the list:
     * <ul>
     * <li>may use wildcards</li>
     * <li>must match (case insensitive) the host name of the URL being requested</li>
     * </ul>
     * <p>
     * Defaults to "localhost, 127.0.0.1"
     * </p>
     * 
     * @param proxyBypassHosts
     * @return
     */
    public HttpEasyDefaults nonProxyHosts(String proxyBypassHosts) {
        HttpEasyDefaults.nonProxyHosts = splitHosts(proxyBypassHosts);

        return this;
    }

    private static List<String> splitHosts(String hosts) {
        return Arrays.asList(Arrays.stream(hosts.split(",")).map(String::trim).map(String::toLowerCase).toArray(String[]::new));
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
            if (bypassProxy && isProxyBypassHost(url)) {
                return Proxy.NO_PROXY;
            }

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

    // Some stuff to think about when we have time...
    // TODO If system system properties http(s).nonProxyHosts are set do we actually need to do anything, documentation says that it's automatically handled.
    // TODO Given that trying to use same nonPorxyHosts config setting for both Browser and HttpEasy is there any difference in format?
    // eg: Do we need to cater for IPAdress ranges? http.nonProxyHosts doesn't specify but I believe browsers can cope with this...
    private static boolean isProxyBypassHost(URL url) {
        String host = url.getHost().toLowerCase();

        return nonProxyHosts.stream().anyMatch(pattern -> host.matches(escapePattern(pattern)));
    }

    private static String escapePattern(String pattern) {
        return pattern.replace(".", "\\.").replace("*", ".*");
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