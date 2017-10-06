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

    private String remoteUserName;
    private String remoteApiKey;

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
        	browserProvider = getProperty("webdriver.browserProvider", "FirefoxBrowserProvider");
        }
        
        if (!browserProvider.contains(".")) {
        	browserProvider = "org.concordion.cubano.driver.web.provider." + browserProvider;
        }
        
        browserSize = getProperty("webdriver.browserSize", null);
        browserMaximized = getPropertyAsBoolean("webdriver.maximized", "false");

        remoteUserName = getProperty("remotewebdriver.userName", null);
        remoteApiKey = getProperty("remotewebdriver.apiKey", null);

        // Yandex HtmlElements automatically implement 5 second implicit wait, default to zero so as not to interfere with
        // explicit waits
        System.setProperty("webdriver.timeouts.implicitlywait", getProperty("webdriver.timeouts.implicitlywait", "0"));
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
}