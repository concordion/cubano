package org.concordion.cubano.driver.web.provider;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

/**
 * Automatically download, configure and start the WebDriver Manager and browser for Microsoft Edge.
 * <p>
 * See https://github.com/bonigarcia/webdrivermanager for details.
 * </p>
 * 
 * @author Andrew Sumner
 */
public class SafariBrowserProvider extends LocalBrowserProvider {
    public static final String BROWSER_NAME = "safari";

    @Override
	protected String getBrowserName() {
		return BROWSER_NAME;
	}
    
    /**
     * @return Starts Safari driver manager and creates a new WebDriver instance.
     */
    @Override
    public WebDriver createDriver() {
        // setupBrowserManager(EdgeDriverManager.getInstance());

        SafariOptions options = new SafariOptions();
        
        addProxyCapabilities(options);

        options.setUseTechnologyPreview(getPropertyAsBoolean("option.useTechnologyPreview", "false"));
        options.setUseCleanSession(getPropertyAsBoolean("option.useCleanSession", "false"));
        
        WebDriver driver = new SafariDriver(options);

        setBrowserSizeAndLocation(driver);

        return driver;
    }
}
