package org.concordion.cubano.driver.web.config;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.concordion.cubano.utils.Config;

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
public class WebDriverConfig extends Config {

    // Browser 
    private String browserProvider;
    private String browserSize;
    private boolean browserMaximized;
    private int restartBrowserTestCount;

    private String remoteUserName;
    private String remoteApiKey;

    // Proxy
    private boolean proxyIsRequired;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String nonProxyHosts;

    protected WebDriverConfig() {
    	super();
    }
    
    protected WebDriverConfig(Properties properties) {
        super(properties);
    }

    protected WebDriverConfig(Properties properties, Properties userProperties) {
        super(properties, userProperties);
    }
       
    private static class WDCHolder {
        static final WebDriverConfig INSTANCE = new WebDriverConfig();
    }

    public static WebDriverConfig getInstance() {
        return WDCHolder.INSTANCE;
    }

    protected void loadProperties() {
        // Browser
        browserProvider = System.getProperty("browserProvider");
        if (browserProvider == null) {
        	browserProvider = getProperty("webdriver.browserProvider");
        }
        
        if (!browserProvider.contains(".")) {
        	browserProvider = "org.concordion.cubano.driver.web.provider." + browserProvider;
        }
        
        restartBrowserTestCount = getPropertyAsInteger("webdriver.resartBrowserTestCount", "0");        
        browserSize = getProperty("webdriver.browserSize", null);
        browserMaximized = getPropertyAsBoolean("webdriver.maximized", "false");

        remoteUserName = getProperty("remotewebdriver.userName", null);
        remoteApiKey = getProperty("remotewebdriver.apiKey", null);

        // Yandex HtmlElements automatically implement 5 second implicit wait, default to zero so as not to interfere with
        // explicit waits
        System.setProperty("webdriver.timeouts.implicitlywait", getProperty("webdriver.timeouts.implicitlywait", "0"));

        // Proxy
        URL proxyUrl = getProxyUrl();
        
        if (proxyUrl != null) {
        	proxyHost = proxyUrl.getHost();
        	proxyPort = String.valueOf(proxyUrl.getPort());
        	        			
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
	        
	        if (proxyUsername == null) { 
	        	proxyUsername = System.getenv("HTTP_PROXY_USER");
	        }
	        
	        if (proxyPassword == null) {
	        	proxyPassword = System.getenv("HTTP_PROXY_PASS");
	        }
	        
	        nonProxyHosts = System.getenv("NO_PROXY");
        }
        
        proxyIsRequired = getPropertyAsBoolean("proxy.required", null);
        proxyHost = proxyIsRequired ? getProperty("proxy.host") : getProperty("proxy.host", System.getProperty("http.proxyHost", proxyHost));
        proxyPort = getProperty("proxy.port", System.getProperty("http.proxyPort", proxyPort));
        proxyUsername = getProperty("proxy.username", System.getProperty("http.proxyUser", proxyUsername));
        proxyPassword = getProperty("proxy.password", System.getProperty("http.proxyPassword", proxyPassword));
        nonProxyHosts = getProperty("proxy.nonProxyHosts", System.getProperty("http.nonProxyHosts", nonProxyHosts)).replaceAll("\\|", ",");
        nonProxyHosts = nonProxyHosts.isEmpty() ? "localhost,127.0.0.1" : nonProxyHosts;

		// Make all WebDriverManager properties system properties
		Map<String, String> result = getPropertiesStartingWith("wdm.");

		for (String key : result.keySet()) {
			System.setProperty(key, result.get(key));
		}
    }

    private URL getProxyUrl() {
    	String proxyInput = System.getenv("HTTP_PROXY");
    	        
        try {
        	if (proxyInput != null) {
        		return new URL(proxyInput.matches("^http[s]?://.*$") ? proxyInput : "http://" + proxyInput);
        	}
        } catch (MalformedURLException e) {
        	// TODO
//            log.error("Invalid proxy url {}", proxyInput, e);
        }
        
        return null;
    }


    public String getBrowserProvider() {
        return browserProvider;
    }

    /**
     * Useful if local browser is not available on path.
     *
     * @return Path to browser executable
     */
    public String getBrowserExe(String browserName) {
    	String localBrowserExe = getProperty(browserName + ".exe", null);
    	
        if (!localBrowserExe.isEmpty()) {
            return localBrowserExe.replace("%USERPROFILE%", System.getProperty("USERPROFILE", ""));
        }

        return "";
    }  
    
    /**
     * Size to set browser window - will default to maximised.
     *
     * @return Size in WxH format
     */
	public String getBrowserSize() {
		return browserSize;
	}

	public boolean isBrowserMaximized() {
		return browserMaximized;
	}
    
    /**
     * Number of tests to execute before restarting browser, a value of <= zero means that it will not be restarted.
     * 
     * This has been added to cater for a memory leak with firefox and gecko driver.
     *
     * @return integer
     */
    public int getRestartBrowserTestCount() {
        return restartBrowserTestCount;
    }
    
    /**
     * Username for remote selenium grid service.
     *
     * @return Username
     */
    public String getRemoteUserName() {
        return remoteUserName;
    }

    /**
     * Api Key to access a remote selenium grid service.
     *
     * @return Api Key
     */
    public String getRemoteApiKey() {
        return remoteApiKey;
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
     * The hostname, or address, of the proxy server.
     */
    public String getProxyHost() {
        return proxyHost;
    }
    
    /**
     * The port number of the proxy server
     */
    public String getProxyPort() {
        return proxyPort;
    }
    
    /**
     * The hostname and port of the proxy server in the format host:port.
     *
     * @return host:port
     */
    public String getProxyAddress() {
    	if (proxyHost.isEmpty()) {
    		return "";
    	}
    	
    	if (proxyPort.isEmpty()) {
    		return proxyHost;
    	}
    	
        return proxyHost + ":" + proxyPort;
    }

    /**
     * Username to authenticate connections through the proxy server 
     *
     * @return username
     */
    public String getProxyUser() {
        return proxyUsername;
    }

    /**
     * Password to authenticate connections through the proxy server.
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
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

}