package org.concordion.cubano.driver.web.provider;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
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

    	// TODO remove capabilities and stick with options
	    DesiredCapabilities capabilities = DesiredCapabilities.firefox();
	    FirefoxOptions options = new FirefoxOptions();
	    
	    options.setLogLevel(FirefoxDriverLogLevel.INFO);
	    options.setLegacy(!useGeckoDriver);
	    	    	    		    	
	    addProxyCapabilities(capabilities);
	
	    if (!WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME).isEmpty()) {
	    	options.setBinary(WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME));
	    }
	    
	    // Profile
	    // TODO Should we default to NEW?
	    String profileName = WebDriverConfig.getInstance().getProperty(BROWSER_NAME + ".profile", "");
	    if (!profileName.equalsIgnoreCase("none")) {
	        FirefoxProfile profile;
	        	        
	        if (profileName.isEmpty()) {
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
	        	        
	        Map<String, String> properties = WebDriverConfig.getInstance().getPropertiesStartingWith("firefox.profile");
	        
	        for (String key : properties.keySet()) {
	        	int start = key.indexOf("[");
	        	int end = key.indexOf("]");
	        	
	        	if (start > 0 && end > start) {
	        		profile.setPreference(key.substring(start + 1, end), properties.get(key));
	        	}
			}
	
			options.setProfile(profile);
	    }

	    options.merge(capabilities);
	    
		WebDriver driver = new FirefoxDriver(options);
		
		setBrowserSize(driver);
 
        return driver;
    }
}
