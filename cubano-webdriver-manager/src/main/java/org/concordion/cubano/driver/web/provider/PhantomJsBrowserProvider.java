package org.concordion.cubano.driver.web.provider;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;

public class PhantomJsBrowserProvider extends LocalBrowserProvider {
	public static final String BROWSER_NAME = "phantomjs";
	
    /**
     * @return Starts PhantomJs driver manager and creates a new WebDriver instance.
     */
	@Override
    public WebDriver createDriver() {
    	setupBrowserManager(PhantomJsDriverManager.getInstance());

        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();

        addProxyCapabilities(capabilities);

        WebDriver driver = new PhantomJSDriver(capabilities);
        
        setBrowserSize(driver);
        
        return driver;
    }

}
