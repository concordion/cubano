package org.concordion.cubano.driver.web.provider;

import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;

import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.OperaDriverManager;

/**
 * Automatically download, configure and start the WebDriver Manager and browser for Opera.
 * <p>
 * See https://github.com/bonigarcia/webdrivermanager for details.
 * </p>
 * 
 * @author Andrew Sumner
 */
public class OperaBrowserProvider extends LocalBrowserProvider {
    public static final String BROWSER_NAME = "opera";

    @Override
	protected String getBrowserName() {
		return BROWSER_NAME;
	}
    
    /**
     * @return Starts Opera driver manager and creates a new WebDriver instance.
     */
    @Override
    public WebDriver createDriver() {
        setupBrowserManager(OperaDriverManager.getInstance(DriverManagerType.OPERA));

        OperaOptions options = new OperaOptions();

        addProxyCapabilities(options);

        if (!getBrowserExe().isEmpty()) {
            options.setBinary(getBrowserExe());
        }

    	// TODO Does this use all same options as chrome, can we extend chrome provider ???  
        addCapabilities(options);

        WebDriver driver = new OperaDriver(options);

        setBrowserSizeAndLocation(driver);

        return driver;
    }

    private void addCapabilities(OperaOptions options) {
        Map<String, String> settings = getPropertiesStartingWith("capability.");

        for (String key : settings.keySet()) {
            options.setCapability(key, toObject(settings.get(key)));
        }
    }
}
