package org.concordion.cubano.driver.web.provider;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.concordion.cubano.driver.web.RemoteHttpClientFactory;
import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.HttpClient.Factory;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Provides everything required to start up a remote browser (desktop or device) - apart from the where to connect.
 * <p>
 * Extend this to provide access to your Selenium grid provider, the framework provides implementations
 * for {@link BrowserStackBrowserProvider BrowserStack}
 * and {@link SauceLabsBrowserProvider SauceLabs}.
 *
 * @author Andrew Sumner
 */
public abstract class RemoteBrowserProvider implements BrowserProvider {
    protected static final String DEFAULT_DESKTOP_SCREENSIZE = "1024x768";
    protected static final String DEFAULT_DESKTOP_VIEWPORT = "950x600";

    private String browser;
    private String viewPort;
    private DesiredCapabilities capabilites;

    /**
     * Default constructor.
     */
    protected RemoteBrowserProvider() {
    }

    /**
     * @return URL to access the remote driver.
     */
    protected abstract String getRemoteDriverUrl();

    /**
     * Constructor.
     * @param browser     Name of the browser
     * @param viewPort    Dimensions of the browser
     * @param capabilites Desired capabilities specific to the selenium grid provider
     */
    protected void setDetails(String browser, String viewPort, DesiredCapabilities capabilites) {
        this.browser = browser;
        this.viewPort = viewPort;
        this.capabilites = capabilites;
    }

    @Override
    public String getBrowser() {
        return browser;
    }

    @Override
    public String getViewPort() {
        return viewPort;
    }

    private DesiredCapabilities getCapabilites() {
        return capabilites;
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebDriver createDriver() {
        URL url;

        try {
            url = new URL(getRemoteDriverUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        WebDriverConfig config = WebDriverConfig.getInstance();

        if (config.isProxyRequired()) {
            HttpClientBuilder builder = HttpClientBuilder.create();
            
            URL proxyURL = getProxyUrl();
                        
            HttpHost proxy = new HttpHost(proxyURL.getHost(), proxyURL.getPort());
            
            builder.setProxy(proxy);
            builder.setDefaultCredentialsProvider(createBasicCredentialsProvider(proxyURL));

            Factory factory = new RemoteHttpClientFactory(builder);

            HttpCommandExecutor executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), url, factory);

            return new RemoteWebDriver(executor, getCapabilites());
        } else {
            return new RemoteWebDriver(url, getCapabilites());
        }
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
    
    private URL getProxyUrl() {
    	WebDriverConfig config = WebDriverConfig.getInstance();
    	
        String proxyInput = isNullOrEmpty(config.getProxyHost()) ? System.getenv("HTTPS_PROXY") : config.getProxyHost();
        if (isNullOrEmpty(proxyInput)) {
            return null;
        }
        try {
            return new URL(proxyInput.matches("^http[s]?://.*$") ? proxyInput : "http://" + proxyInput);
        } catch (MalformedURLException e) {
        	// TODO
//            log.error("Invalid proxy url {}", proxyInput, e);
            return null;
        }
    }

    private final BasicCredentialsProvider createBasicCredentialsProvider(URL proxyURL) {
    	if (proxyURL == null) {
            return null;
        }
        
        WebDriverConfig config = WebDriverConfig.getInstance();
    	
        try {
        	String username = null;
        	String password = null;

            // apply env value
            String userInfo = proxyURL.getUserInfo();
            if (userInfo != null) {
                StringTokenizer st = new StringTokenizer(userInfo, ":");
                username = st.hasMoreTokens() ? URLDecoder.decode(st.nextToken(), StandardCharsets.UTF_8.name()) : null;
                password = st.hasMoreTokens() ? URLDecoder.decode(st.nextToken(), StandardCharsets.UTF_8.name()) : null;
            }
            
            if (username == null) {
            	username = isNullOrEmpty(config.getProxyUser()) ? System.getenv("HTTPS_PROXY_USER") : config.getProxyUser();
            }
            if (password == null) {
            	password = isNullOrEmpty(config.getProxyPassword()) ? System.getenv("HTTPS_PROXY_PASS") : config.getProxyPassword();
            }

            if (username == null) {
                return null;
            }

            String ntlmUsername = username;
            String ntlmDomain = null;
            
            int index = username.indexOf("\\");
            if (index > 0) {
                ntlmDomain = username.substring(0, index);
                ntlmUsername = username.substring(index + 1);                
            }
            
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            Credentials creds;
            AuthScope authScope;
            
            authScope = new AuthScope(proxyURL.getHost(), proxyURL.getPort(),  AuthScope.ANY_REALM, AuthSchemes.NTLM);
            creds = new NTCredentials(ntlmUsername, password, getWorkstation(), ntlmDomain);            
            credentialsProvider.setCredentials(authScope, creds);
            
            authScope = new AuthScope(proxyURL.getHost(), proxyURL.getPort());
            creds = new UsernamePasswordCredentials(username, password);
            credentialsProvider.setCredentials(authScope, creds);

            return credentialsProvider;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Invalid encoding.", e);
        }
    }

    private String getWorkstation() {
        Map<String, String> env = System.getenv();

        if (env.containsKey("COMPUTERNAME")) {
            // Windows
            return env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            // Unix/Linux/MacOS
            return env.get("HOSTNAME");
        } else {
            // From DNS
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                return null;
            }
        }
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj The object to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    public boolean equals(BrowserProvider obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        RemoteBrowserProvider compare = (RemoteBrowserProvider) obj;

        if (!areEqual(this.getCapabilites(), compare.getCapabilites())) {
            return false;
        }
        if (!areEqual(this.getBrowser(), compare.getBrowser())) {
            return false;
        }
        if (!areEqual(this.getViewPort(), compare.getViewPort())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.browser, this.capabilites, this.viewPort);
    }

    @Override
    public boolean isViewPortDefined() {
        return getViewPort() != null && !getViewPort().isEmpty();
    }

    @Override
    public int getViewPortWidth() {
        if (!isViewPortDefined()) {
            return -1;
        }

        String width = getViewPort().substring(0, getViewPort().indexOf("x")).trim();

        return Integer.parseInt(width);
    }

    @Override
    public int getViewPortHeight() {
        if (!isViewPortDefined()) {
            return -1;
        }

        String height = getViewPort().substring(getViewPort().indexOf("x") + 1).trim();

        return Integer.parseInt(height);
    }

    private boolean areEqual(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        if (obj2 == null) {
            return false;
        }

        return obj1.equals(obj2);
    }
}