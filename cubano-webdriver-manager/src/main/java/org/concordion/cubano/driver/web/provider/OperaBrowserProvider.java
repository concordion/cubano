package org.concordion.cubano.driver.web.provider;

import java.util.Map;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;

import io.github.bonigarcia.wdm.OperaDriverManager;

public class OperaBrowserProvider extends LocalBrowserProvider {
    public static final String BROWSER_NAME = "opera";

    /**
     * @return Starts Opera driver manager and creates a new WebDriver instance.
     */
    @Override
    public WebDriver createDriver() {
        setupBrowserManager(OperaDriverManager.getInstance());

        OperaOptions options = new OperaOptions();

        addProxyCapabilities(options);

        if (!WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME).isEmpty()) {
            options.setBinary(WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME));
        }

        addCapabilities(options);

        WebDriver driver = new OperaDriver(options);

        setBrowserSize(driver);

        return driver;
    }

    private void addCapabilities(OperaOptions options) {
        Map<String, String> settings = WebDriverConfig.getInstance().getPropertiesStartingWith("opera.capability.", true);

        for (String key : settings.keySet()) {
            options.setCapability(key, settings.get(key));
        }
    }
}
