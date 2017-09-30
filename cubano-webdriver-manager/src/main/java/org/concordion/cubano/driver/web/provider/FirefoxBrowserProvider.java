package org.concordion.cubano.driver.web.provider;

import java.io.File;
import java.util.Map;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
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
    	boolean useGeckoDriver = WebDriverConfig.getInstance().getPropertyAsBoolean(BROWSER_NAME + ".useGeckoDriver", "true");
    	
    	if (useGeckoDriver) {
    		setupBrowserManager(FirefoxDriverManager.getInstance());
    	}

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();

        addProxyCapabilities(capabilities);

        if (!WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME).isEmpty()) {
            capabilities.setCapability(FirefoxDriver.BINARY, WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME));
        }
        
        capabilities.setCapability("marionette", useGeckoDriver);
        
        // Profile
	    String profileName = WebDriverConfig.getInstance().getProperty(BROWSER_NAME + ".profile", "");
	    if (!profileName.isEmpty()) {
	        FirefoxProfile profile;
	        	        
	        if (profileName.equals("new")) {
	        	profile = new FirefoxProfile();
	        } else {        	
		        profile = new ProfilesIni().getProfile(profileName);
		        if (profile == null) {
		        	File folder = new File(profileName);
		        	if (folder.exists() && folder.isDirectory()) {
		        		profile = new FirefoxProfile(folder);
		        	} else {
		        		throw new InvalidArgumentException(profileName + " does not match an existing Firefox profile or folder");
		        	}	        	
		        }
	        }
	        
	        if (useGeckoDriver) {
				// Work around for FireFox not closing, fix comes from here: https://github.com/mozilla/geckodriver/issues/517
				profile.setPreference("browser.tabs.remote.autostart", false);
				profile.setPreference("browser.tabs.remote.autostart.1", false);
				profile.setPreference("browser.tabs.remote.autostart.2", false);
				profile.setPreference("browser.tabs.remote.force-enable", false);
	        }
	        
	        Map<String, String> properties = WebDriverConfig.getInstance().getPropertiesStartingWith("firefox.profile");
	        
	        for (String key : properties.keySet()) {
	        	int start = key.indexOf("[");
	        	int end = key.indexOf("]");
	        	
	        	if (start > 0 && end > start) {
	        		profile.setPreference(key.substring(start + 1, end), properties.get(key));
	        	}
			}

			capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        }

		WebDriver driver = new FirefoxDriver(capabilities);
		
		setBrowserSize(driver);
 
        return driver;
    }
}
