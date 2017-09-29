package org.concordion.cubano.driver.web.provider;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.EdgeDriverManager;

public class EdgeBrowserProvider extends LocalBrowserProvider {
    
    /**
     * @return Starts Edge driver manager and creates a new WebDriver instance.
     */
	@Override
	public WebDriver createDriver() {
    	setupBrowserManager(EdgeDriverManager.getInstance());

        DesiredCapabilities capabilities = DesiredCapabilities.edge();

        addProxyCapabilities(capabilities);

        WebDriver driver = new EdgeDriver(capabilities);
        
        setBrowserSize(driver);
        
        return driver;
    }

    
}
