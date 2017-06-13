package org.concordion.cubano.driver.http;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Allows setting of default properties used by all subsequent HttpEasy requests.
 *
 * @author Andrew Sumner
 */
public class HttpEasyDefaults {
    // Static values are set by RestRequestDefaults and apply to all requests
    private static Proxy proxy = Proxy.NO_PROXY;
    private static String proxyUser = null;
    private static String proxyPassword = null;
    private static boolean bypassProxyForLocalAddresses = true;
    private static String baseURI = "";
    private static LogWriter defaultLogWriter = null;

    /**
     * Create all-trusting certificate verifier.
     *
     * @return A self reference
     */
    public HttpEasyDefaults trustAllCertificates() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            HttpEasy.LOGGER.error(e.getMessage());
        }

        return this;
    }

    /**
     * Create all-trusting host name verifier.
     *
     * @return A self reference
     */
    public HttpEasyDefaults allowAllHosts() {
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

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
}