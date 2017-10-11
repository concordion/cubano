package org.concordion.cubano.driver.web.provider;

import java.util.Map;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

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

    /**
     * @return Starts Edge driver manager and creates a new WebDriver instance.
     */
    @Override
    public WebDriver createDriver() {
        setupBrowserManager(EdgeDriverManager.getInstance());

        EdgeOptions options = new EdgeOptions();

        addProxyCapabilities(options);
        addCapabilities(options);

        WebDriver driver = new EdgeDriver(options);

        setBrowserSize(driver);

        return driver;
    }

    private void addCapabilities(EdgeOptions options) {
        Map<String, String> settings = WebDriverConfig.getInstance().getPropertiesStartingWith("edge.capability.", true);

        for (String key : settings.keySet()) {
            options.setCapability(key, settings.get(key));
        }
    }

}
