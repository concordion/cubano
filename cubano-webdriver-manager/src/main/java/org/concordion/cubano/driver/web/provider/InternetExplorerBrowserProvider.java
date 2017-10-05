package org.concordion.cubano.driver.web.provider;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.InternetExplorerDriverManager;

public class InternetExplorerBrowserProvider extends LocalBrowserProvider {
	public static final String BROWSER_NAME = "ie";
    
    /**
     * @return Starts Internet Explorer driver manager and creates a new WebDriver instance.
     */
	@Override
	public WebDriver createDriver() {
    	setupBrowserManager(InternetExplorerDriverManager.getInstance());

        InternetExplorerOptions options = new InternetExplorerOptions();

        // capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        // "ignore", "accept", or "dismiss".
        // capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "dismiss");

        // capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

        addProxyCapabilities(options);

        WebDriver driver = new InternetExplorerDriver(options);
        
        setBrowserSize(driver);
        
        return driver;
    }
}
