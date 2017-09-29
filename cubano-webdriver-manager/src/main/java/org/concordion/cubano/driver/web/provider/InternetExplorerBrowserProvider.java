package org.concordion.cubano.driver.web.provider;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
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

        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();

        // capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        // "ignore", "accept", or "dismiss".
        // capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "dismiss");

        // capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

        addProxyCapabilities(capabilities);

        WebDriver driver = new InternetExplorerDriver(capabilities);
        
        setBrowserSize(driver);
        
        return driver;
    }
}
