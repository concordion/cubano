package org.concordion.cubano.driver.web.provider;

import java.io.File;
import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.FirefoxDriverManager;

/**
 * Provides everything required to start up a local desktop browser, currently supports chrome, ie and firefox
 * <p>
 * Browser drivers are automatically downloaded as required when requesting a browser: see https://github.com/bonigarcia/webdrivermanager for details
 * TODO: How configure proxy, download location and other customisation
 *
 * @author Andrew Sumner
 */
public class FirefoxBrowserProvider extends LocalBrowserProvider {
    public static final String BROWSER_NAME = "firefox";
    
	/**
     * For running portable firefox at same time as desktop version:
     *      1. Edit FirefoxPortable.ini (next to FirefoxPortable.exe)
     *      2. If its not there then copy from "Other/Source" folder
     *      3. Change AllowMultipleInstances=false to true
     *
     * @return Starts FireFox driver manager and creates a new WebDriver instance.
     */

    @Override
    public WebDriver createDriver() {
    	boolean useGeckoDriver = WebDriverConfig.getInstance().getPropertyAsBoolean("webdriver." + BROWSER_NAME + ".useGeckoDriver", "true");
    	
    	if (useGeckoDriver) {
    		setupBrowserManager(FirefoxDriverManager.getInstance());
    	}

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();

        addProxyCapabilities(capabilities);

        if (!WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME).isEmpty()) {
            capabilities.setCapability(FirefoxDriver.BINARY, WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME));
        }
        
        capabilities.setCapability("marionette", useGeckoDriver);
        
		// Work around for FireFox not closing, fix comes from here: https://github.com/mozilla/geckodriver/issues/517
		FirefoxProfile profile = new FirefoxProfile();
		
		profile.setPreference("browser.tabs.remote.autostart", false);
		profile.setPreference("browser.tabs.remote.autostart.1", false);
		profile.setPreference("browser.tabs.remote.autostart.2", false);
		profile.setPreference("browser.tabs.remote.force-enable", false);

		// Include Plugins
        if (WebDriverConfig.getInstance().shouldActivatePlugins(BROWSER_NAME)) {
            try {
                File firebug = Plugins.get("firebug");
                profile.addExtension(firebug);

                String version = firebug.getName();
                version = version.substring(version.indexOf("-") + 1);
                version = version.substring(0, version.indexOf("-") > 0 ? version.indexOf("-") : version.indexOf("."));

                profile.setPreference("extensions.firebug.currentVersion", version);

                profile.addExtension(Plugins.get("firepath"));
            } catch (Exception e) {
                throw new RuntimeException("Unable to add FireFox plugins", e);
            }
		}

		capabilities.setCapability(FirefoxDriver.PROFILE, profile);

		WebDriver driver = new FirefoxDriver(capabilities);
		
		setBrowserSize(driver);
 
        return driver;
    }
}
