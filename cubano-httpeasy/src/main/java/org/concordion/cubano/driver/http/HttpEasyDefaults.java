package org.concordion.cubano.driver.http;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import javax.net.ssl.HttpsURLConnection;

/**
 * Allows setting of default properties used by all subsequent HttpEasy requests.
 *
 * @author Andrew Sumner
 */
public class HttpEasyDefaults {
    // Static values are set by RestRequestDefaults and apply to all requests
    private static boolean trustAllCertificates = false;
    private static Proxy proxy = Proxy.NO_PROXY;
    private static String proxyUser = null;
    private static String proxyPassword = null;
    private static boolean bypassProxyForLocalAddresses = true;
    private static String baseURI = "";
    private static LogWriter defaultLogWriter = null;

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
     * Create a global all-trusting certificate verifier.
     * This might have unintended consequences and you should consider using the {@link #trustAllCertificates(boolean)} instead.
     * 
     * @return A self reference
     */
    public HttpEasyDefaults trustAllCertificates() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(SSLUtilities.getTrustAllCertificatesSocketFactory());
        } catch (Exception e) {
            HttpEasy.LOGGER.error(e.getMessage());
        }

        return this;
    }

    /**
     * Create all-trusting host name verifier.
     * This might have uninteded consequences and you should consider using the {@link #trustAllCertificates(boolean)} instead.
     * 
     * @return A self reference
     */
    public HttpEasyDefaults allowAllHosts() {
        HttpsURLConnection.setDefaultHostnameVerifier(SSLUtilities.getTrustAllHostsVerifier());

        return this;
    }

    /**
     * Add default authorization.
     *
     * @param username username if need NTLM authentication format would be DOMAIN\\user
     * @param password password
     * @return A self reference
     */
    public HttpEasyDefaults authorization(final String username, final String password) {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        });

        return this;
    }

    /**
     * Set an entry representing a PROXY connection.
     *
     * @param proxy Sets the default {@link Proxy} to use for all connections
     * @return A self reference
     */
    public HttpEasyDefaults proxy(Proxy proxy) {
        HttpEasyDefaults.setProxy(proxy);
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
        HttpEasyDefaults.setProxyAuth(userName, password);
        return this;
    }

    /**
     * Use proxy, or not, for local addresses.
     *
     * @param bypassLocalAddresses Value
     * @return A self reference
     */
    public HttpEasyDefaults bypassProxyForLocalAddresses(boolean bypassLocalAddresses) {
        HttpEasyDefaults.setBypassProxyForLocalAddresses(bypassLocalAddresses);
        return this;
    }

    /**
     * Set the default base url for all HttpEasy requests.
     *
     * @param baseUrl Base URL
     * @return A self reference
     */
    public HttpEasyDefaults baseUrl(String baseUrl) {
        HttpEasyDefaults.setBaseUrl(baseUrl);
        return this;
    }

    /**
     * Set the default logger to write to.
     *
     * @param logWriter Log writer implementation
     * @return A self reference
     */
    public HttpEasyDefaults withLogWriter(LogWriter logWriter) {
        HttpEasyDefaults.setDefaultLogWriter(logWriter);
        return this;
    }

    public static Proxy getProxy() {
        return HttpEasyDefaults.proxy;
    }

    public static String getProxyUser() {
        return HttpEasyDefaults.proxyUser;
    }

    public static String getProxyPassword() {
        return HttpEasyDefaults.proxyPassword;
    }

    public static boolean isBypassProxyForLocalAddresses() {
        return HttpEasyDefaults.bypassProxyForLocalAddresses;
    }

    public static String getBaseURI() {
        return HttpEasyDefaults.baseURI;
    }

    public static LogWriter getDefaultLogWriter() {
        return HttpEasyDefaults.defaultLogWriter;
    }

    private static void setBaseUrl(String baseUrl) {
        HttpEasyDefaults.baseURI = baseUrl;
    }

    private static void setDefaultLogWriter(LogWriter logWriter) {
        HttpEasyDefaults.defaultLogWriter = logWriter;
    }

    private static void setProxy(Proxy proxy) {
        HttpEasyDefaults.proxy = proxy;
    }

    private static void setProxyAuth(String userName, String password) {
        HttpEasyDefaults.proxyUser = userName;
        HttpEasyDefaults.proxyPassword = password;
    }

    private static void setBypassProxyForLocalAddresses(boolean bypassLocalAddresses) {
        HttpEasyDefaults.bypassProxyForLocalAddresses = bypassLocalAddresses;
    }

    public static boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }
}