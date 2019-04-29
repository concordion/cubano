package org.concordion.cubano.driver.web.provider;

import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.CapabilityType;

import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;

/**
 * Automatically download, configure and start the WebDriver Manager and browser for Microsoft Internet Explorer.
 * <p>
 * See https://github.com/bonigarcia/webdrivermanager for details.
 * </p>
 * 
 * @author Andrew Sumner
 */
public class InternetExplorerBrowserProvider extends LocalBrowserProvider {
    public static final String BROWSER_NAME = "ie";

    @Override
	protected String getBrowserName() {
		return BROWSER_NAME;
	}
    
    /**
     * @return Starts Internet Explorer driver manager and creates a new WebDriver instance.
     */
    @Override
    public WebDriver createDriver() {
        setupBrowserManager(InternetExplorerDriverManager.getInstance(DriverManagerType.IEXPLORER));

        InternetExplorerOptions options = new InternetExplorerOptions();

        // capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        // "ignore", "accept", or "dismiss".
        // capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "dismiss");

        // capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

        addProxyCapabilities(options);
        addRecommendedDefaultCapabilities(options);
        addCapabilities(options);

        WebDriver driver = new InternetExplorerDriver(options);

        setBrowserSizeAndLocation(driver);

        return driver;
    }

    private void addCapabilities(InternetExplorerOptions options) {
        Map<String, String> settings = getPropertiesStartingWith("capability.");

        for (String key : settings.keySet()) {
            options.setCapability(key, toObject(settings.get(key)));
        }
    }

    private void addRecommendedDefaultCapabilities(InternetExplorerOptions options) {
        options.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
        options.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
        options.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, false);
        options.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
        options.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
    }
}
