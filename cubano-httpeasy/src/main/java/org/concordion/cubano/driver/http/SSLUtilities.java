package org.concordion.cubano.driver.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLUtilities {

    private static TrustManager[] trustAllCerts = null;
    private static SSLSocketFactory trustAllSocketFactory = null;
    private static HostnameVerifier allHostsValid = null;

    public static SSLSocketFactory getTrustAllCertificatesSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {

        if (trustAllCerts == null) {
            // Create a trust manager that does not validate certificate chains
            trustAllCerts = new TrustManager[] {
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
        }

        if (trustAllSocketFactory == null) {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            
            trustAllSocketFactory = sc.getSocketFactory();
        }

        return trustAllSocketFactory;
    }

    public static HostnameVerifier getTrustAllHostsVerifier() {
        if (allHostsValid == null) {
            allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
        }

        return allHostsValid;
    }
}
