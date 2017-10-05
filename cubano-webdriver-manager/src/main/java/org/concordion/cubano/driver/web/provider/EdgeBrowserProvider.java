package org.concordion.cubano.driver.web.provider;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.EdgeDriverManager;

public class EdgeBrowserProvider extends LocalBrowserProvider {
    
    /**
     * @return Starts Edge driver manager and creates a new WebDriver instance.
     */
	@Override
	public WebDriver createDriver() {
    	setupBrowserManager(EdgeDriverManager.getInstance());

    	EdgeOptions options = new EdgeOptions();
        
        addProxyCapabilities(options);

        WebDriver driver = new EdgeDriver(options);
        
        setBrowserSize(driver);
        
        return driver;
    }

    
}
