package org.concordion.cubano.driver.web.config;

import java.util.Map;
import java.util.Properties;

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
    private int browserDefaultTimeout;
    private int restartBrowserTestCount;

    private String remoteUserName;
    private String remoteApiKey;

    // Proxy
    private boolean proxyIsRequired;
    private String proxyHost;
    private String proxyUsername;
    private String proxyPassword;

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
        browserDefaultTimeout = getPropertyAsInteger("webdriver.defaultTimeout", "0");
        browserSize = getProperty("webdriver.browserSize", null);
        browserMaximized = getPropertyAsBoolean("webdriver.maximized", "false");

        remoteUserName = getProperty("remotewebdriver.userName", null);
        remoteApiKey = getProperty("remotewebdriver.apiKey", null);

        // Yandex HtmlElements automatically implement 5 second implicit wait, default to zero so as not to interfere with
        // explicit waits
        System.setProperty("webdriver.timeouts.implicitlywait", getProperty("webdriver.timeouts.implicitlywait", "0"));

        // Proxy
        proxyIsRequired = getPropertyAsBoolean("proxy.required", null);

        proxyHost = proxyIsRequired ? getProperty("proxy.host") : getProperty("proxy.host", null);
        proxyUsername = getProperty("proxy.username", null);
        proxyPassword = getProperty("proxy.password", null);

		// Make all WebDriverManager properties system properties
		Map<String, String> result = getPropertiesStartingWith("wdm.");

		for (String key : result.keySet()) {
			System.setProperty(key, result.get(key));
		}
    }

    public String getBrowserProvider() {
        return browserProvider;
    }

    public boolean shouldActivatePlugins(String browserName) {
    	return WebDriverConfig.getInstance().getPropertyAsBoolean(browserName + ".activatePlugins", null);
    }
    
    /**
     * Useful if local browser is not available on path.
     *
     * @return Path to browser executable
     */
    public String getBrowserExe(String browserName) {
    	String localBrowserExe = WebDriverConfig.getInstance().getProperty(browserName + ".exe", null);
    	
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
     * Default timeout in seconds.
     *
     * @return timeout
     */
    public int getDefaultTimeout() {
        return browserDefaultTimeout;
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
     * Proxy should be setup or not.
     *
     * @return true or false
     */
    public boolean isProxyRequired() {
        return proxyIsRequired;
    }

    /**
     * Proxy host name.
     *
     * @return host
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Proxy username.
     *
     * @return username
     */
    public String getProxyUser() {
// TODO: Not sure that proxy user should fail if not provided
//        if (proxyUsername == null) {
//            throw new RuntimeException("proxy.username entry must exist in the user.properties file in the root folder");
//        }

        return proxyUsername;
    }

    /**
     * Proxy user.
     *
     * @return user
     */
    public String getProxyPassword() {
//  TODO: Not sure that proxy password should fail if not provided
//        if (proxyPassword == null) {
//            throw new RuntimeException("proxy.proxypassword entry must exist in the user.properties file in the root folder");
//        }

        return proxyPassword;
    }

    /**
     * @return Proxy bypass (noproxy) addresses, eg: "localhost, 127.0.0.1".
     */
    public String getNoProxyList() {
        return "localhost, 127.0.0.1";
    }

}