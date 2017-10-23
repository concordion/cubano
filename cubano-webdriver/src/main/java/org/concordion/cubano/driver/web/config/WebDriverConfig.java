package org.concordion.cubano.driver.web.config;

import java.util.Properties;

import org.concordion.cubano.config.Config;
import org.concordion.cubano.config.DefaultPropertiesLoader;
import org.concordion.cubano.config.DefaultPropertyLoader;
import org.concordion.cubano.config.PropertiesLoader;

/**
 * Reads and supplies properties from the <code>config.properties</code> file that are required by the framework.
 * <p>
 * An optional <code>user.properties</code> file can set user specific values and allow overriding of defaults.
 * The <code>user.properties</code> file should NEVER be checked into source control.
 *
 * @author Andrew Sumner
 */
public final class WebDriverConfig {

    private final DefaultPropertyLoader propertyLoader;

    // Browser
    private String browserProvider;
    private String browserDimension;
    private String browserPosition;
    private boolean browserMaximized;
    private boolean eventLoggingEnabled;

    private String remoteUserName;
    private String remoteApiKey;

    private static class WDCHolder {
        static final WebDriverConfig INSTANCE = new WebDriverConfig();
    }

    /**
     * @return singleton instance
     */
    public static WebDriverConfig getInstance() {
        return WDCHolder.INSTANCE;
    }

    /**
     * Uses DefaultPropertiesLoader to import the config and user properties files.
     */
    protected WebDriverConfig() {
        this(DefaultPropertiesLoader.getInstance());
    }

    /**
     * Uses the supplied PropertiesLoader to import the config and user properties files.
     *
     * @param propertiesLoader Configuration loader
     */
    protected WebDriverConfig(PropertiesLoader propertiesLoader) {
        this(propertiesLoader.getProperties(), propertiesLoader.getUserProperties());
    }

    /**
     * Allow injection of properties for testing purposes.
     *
     * @param properties Default properties
     */
    protected WebDriverConfig(Properties properties) {
        this(properties, null);
    }

    /**
     * Allow injection of properties for testing purposes.
     *
     * @param properties Default properties
     * @param userProperties User specific overrides
     */
    protected WebDriverConfig(Properties properties, Properties userProperties) {
        propertyLoader = new DefaultPropertyLoader(properties, userProperties);

        loadProperties();
    }

    /**
     * @return a loader for loading properties across config.properties and user.properties, taking environment into account.
     */
    public DefaultPropertyLoader getPropertyLoader() {
        return propertyLoader;
    }

    private void loadProperties() {
        // Browser
        browserProvider = System.getProperty("browserProvider");
        if (browserProvider == null) {
            browserProvider = propertyLoader.getProperty("webdriver.browserProvider", "FirefoxBrowserProvider");
        }

        if (!browserProvider.contains(".")) {
            browserProvider = "org.concordion.cubano.driver.web.provider." + browserProvider;
        }

        browserDimension = propertyLoader.getProperty("webdriver.browser.dimension", null);
        browserPosition = propertyLoader.getProperty("webdriver.browser.position", null);
        browserMaximized = propertyLoader.getPropertyAsBoolean("webdriver.browser.maximized", "false");
        eventLoggingEnabled = propertyLoader.getPropertyAsBoolean("webdriver.event.logging", "true");

        remoteUserName = propertyLoader.getProperty("remotewebdriver.userName", null);
        remoteApiKey = propertyLoader.getProperty("remotewebdriver.apiKey", null);

        // Yandex HtmlElements automatically implement 5 second implicit wait, default to zero so as not to interfere with
        // explicit waits
        System.setProperty("webdriver.timeouts.implicitlywait", propertyLoader.getProperty("webdriver.timeouts.implicitlywait", "0"));
    }

    public String getBrowserProvider() {
        return browserProvider;
    }

    /**
     * Position to locate browser window.
     *
     * @return Size in WxH format
     */
    public String getBrowserPosition() {
        return browserPosition;
    }

    /**
     * Size to set browser window.
     *
     * @return Size in WxH format
     */
    public String getBrowserDimension() {
        return browserDimension;
    }

    /**
     * Browser should be maximized or not.
     */
    public boolean isBrowserMaximized() {
        return browserMaximized;
    }

    /**
     * Selenium WebDriver logging should be enabled.
     */
    public boolean isEventLoggingEnabled() {
        return eventLoggingEnabled;
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