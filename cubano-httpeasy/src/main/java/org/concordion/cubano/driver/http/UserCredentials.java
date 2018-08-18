package org.concordion.cubano.driver.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.util.Base64;

import waffle.windows.auth.IWindowsAccount;
import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.impl.WindowsAccountImpl;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;


public class UserCredentials {
    // TODO ANDREW TESTING AUTO CONFIG PROXY AUTHENTICATION
    // https://github.com/Waffle/waffle/blob/master/Docs/faq/ClientSide.md
    // https://github.com/Waffle/waffle/blob/master/Source/JNA/waffle-tests/src/test/java/waffle/servlet/NegotiateSecurityFilterTests.java#L133
    // This implementation using 'com.github.waffle:waffle-jna:1.9.1', if get it working might want to re-engineer so only importing JNA as there were some extra libraries I
    // was unsure about
    // Should try Negotiate so only sending credentials when actually need to

    // https://github.com/Waffle/waffle/blob/master/Docs/GettingStartedWithWaffleAPI.md

    public void getCredentials(HttpURLConnection request) {
        WindowsAuthProviderImpl waffle = new WindowsAuthProviderImpl();
        IWindowsAccount currentUser = waffle.lookupAccount(WindowsAccountImpl.getCurrentUsername());
        System.out.println(currentUser.getSidString());
        System.out.println(currentUser.getFqn());

        final String securityPackage = "NEGOTIATE"; // NegotiateSecurityFilter.NEGOTIATE;

        IWindowsCredentialsHandle clientCredentials = null;

        WindowsSecurityContextImpl clientContext = null;

        try {

            // client credentials handle

            clientCredentials = WindowsCredentialsHandleImpl.getCurrent(securityPackage);
            clientCredentials.initialize();

            // initial client security context
            clientContext = new WindowsSecurityContextImpl();
            clientContext.setPrincipalName(WindowsAccountImpl.getCurrentUsername());
            clientContext.setCredentialsHandle(clientCredentials);
            clientContext.setSecurityPackage(securityPackage);
            clientContext.initialize(null, null, WindowsAccountImpl.getCurrentUsername());

            // final SimpleHttpRequest request = new SimpleHttpRequest();

            // request.setMethod("POST");
            request.setRequestMethod("GET");
            // request.setContentLength(0);

            final String clientToken = Base64.getEncoder().encodeToString(clientContext.getToken());

            // request.addHeader("Authorization", securityPackage + " " + clientToken);
            request.setRequestProperty("Proxy-Authorization", securityPackage + " " + clientToken);

            // final SimpleHttpResponse response = new SimpleHttpResponse();
            // this.filter.doFilter(request, response, null);

            request.connect();

            // assertTrue(response.getHeader("WWW-Authenticate").startsWith(securityPackage + " "));
            // assertEquals("keep-alive", response.getHeader("Connection"));
            // assertEquals(2, response.getHeaderNamesSize());
            // assertEquals(401, response.getStatus());

            assertTrue(request.getHeaderField("WWW-Authenticate").startsWith(securityPackage + " "));
            assertEquals("keep-alive", request.getHeaderField("Connection"));
            // assertEquals(2, response.getHeaderNamesSize());
            assertEquals(401, request.getResponseCode());

        } catch (Exception e) {
            System.out.println(e);

        } finally {
            if (clientContext != null) {
                clientContext.dispose();
            }

            if (clientCredentials != null) {
                clientCredentials.dispose();
            }
        }
    }
}
