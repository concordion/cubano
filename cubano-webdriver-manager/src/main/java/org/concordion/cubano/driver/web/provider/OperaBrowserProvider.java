package org.concordion.cubano.driver.web.provider;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

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

        WebDriver driver = new OperaDriver(options);
        
        setBrowserSize(driver);
        
        return driver;
    }
}
