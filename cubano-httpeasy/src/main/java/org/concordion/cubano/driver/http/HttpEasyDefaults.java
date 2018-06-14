package org.concordion.cubano.driver.http;

import java.net.Proxy;
import java.net.URL;

/**
 * Allows setting of default properties used by all subsequent HttpEasy requests.
 *
 * @author Andrew Sumner
 */
public class HttpEasyDefaults {
    private static String baseUrl = "";
    private static boolean trustAllCertificates = false;

    // Request authorisation
    private static String authUser = null;
    private static String authPassword = null;

    // Proxy
    private static ProxyDetection proxyDetection = ProxyDetection.AUTO;
    private static volatile ProxySearch proxySearch = null;
    private static Proxy proxy = Proxy.NO_PROXY;
    private static String proxyUser = null;
    private static String proxyPassword = null;

    // Logging
    private static LogWriter defaultLogWriter = new LoggerLogWriter();
    private static boolean logRequestDetails = false;

    /**
     * Create all-trusting certificate and host name verifier per HTTPS request.
     * 
     * @param trustAllCertificates Set to true to trust all certificates, the default is false
     * @return A self reference
     */
    public HttpEasyDefaults trustAllCertificates(boolean trustAllCertificates) {
        HttpEasyDefaults.trustAllCertificates = trustAllCertificates;

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

    public HttpEasyDefaults logRequestDetails() {
        HttpEasyDefaults.logRequestDetails = true;

        return this;
    }

    public static boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    public static String getAuthUser() {
        return HttpEasyDefaults.authUser;
    }

    public static String getAuthPassword() {
        return HttpEasyDefaults.authPassword;
    }

    public static Proxy getProxy(URL url) {
        if (proxyDetection != ProxyDetection.AUTO) {
            return proxy;
        }

        if (proxySearch == null) {
            synchronized (HttpEasyDefaults.class) {
                if (proxySearch == null) {
                    proxySearch = new ProxySearch();
                }
            }
        }

        return proxySearch.select(url);
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

    public static boolean getLogRequestDetails() {
        return logRequestDetails;
    }
}