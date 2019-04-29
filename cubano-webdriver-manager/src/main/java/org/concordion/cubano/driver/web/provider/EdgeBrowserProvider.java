package org.concordion.cubano.driver.web.provider;

import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.EdgeDriverManager;

/**
 * Automatically download, configure and start the WebDriver Manager and browser for Microsoft Edge.
 * <p>
 * See https://github.com/bonigarcia/webdrivermanager for details.
 * </p>
 * 
 * @author Andrew Sumner
 */
public class EdgeBrowserProvider extends LocalBrowserProvider {
    public static final String BROWSER_NAME = "edge";

    @Override
	protected String getBrowserName() {
		return BROWSER_NAME;
	}
    
    /**
     * @return Starts Edge driver manager and creates a new WebDriver instance.
     */
    @Override
    public WebDriver createDriver() {
        setupBrowserManager(EdgeDriverManager.getInstance(DriverManagerType.EDGE));

        EdgeOptions options = new EdgeOptions();

        addProxyCapabilities(options);
        addCapabilities(options);

        WebDriver driver = new EdgeDriver(options);

        setBrowserSizeAndLocation(driver);

        return driver;
    }

    private void addCapabilities(EdgeOptions options) {
        Map<String, String> settings = getPropertiesStartingWith("capability.");

        for (String key : settings.keySet()) {
            options.setCapability(key, toObject(settings.get(key)));
        }
    }
}
