package org.concordion.cubano.driver.web.provider;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;

/**
 * Automatically download, configure and start the WebDriver Manager and browser for Chrome.
 * <p>
 * See https://github.com/bonigarcia/webdrivermanager for details.
 * </p>
 * 
 * @author Andrew Sumner
 */
public class ChromeBrowserProvider extends LocalBrowserProvider {
    public static final String BROWSER_NAME = "chrome";

	@Override
	protected String getBrowserName() {
		return BROWSER_NAME;
	}
	
    /**
     * @return Starts Chrome driver manager and creates a new WebDriver instance.
     */
    @Override
    public WebDriver createDriver() {
        setupBrowserManager(WebDriverManager.getInstance(CHROME));

        ChromeOptions options = new ChromeOptions();

        addProxyCapabilities(options);

        if (!getBrowserExe().isEmpty()) {
            options.setBinary(getBrowserExe());
        }

        setBrowserSizeAndLocation(options);
        addCapabilities(options);
        addArguments(options);
        addOptions(options);
        addPreferences(options);
        addExtensions(options);

        options.setHeadless(getPropertyAsBoolean("headless", "false"));

        WebDriver driver = new ChromeDriver(options);

        return driver;
    }

    /**
     * While Chrome supports the standard driver.manage().window() WebDriver settings, it also supports arguments which work a little nicer.
     */
	private void setBrowserSizeAndLocation(ChromeOptions options) {
		WebDriverConfig config = WebDriverConfig.getInstance();
		
        if (config.isBrowserMaximized()) {
        	options.addArguments("start-maximized");
        } else {
            if (!config.getBrowserDimension().isEmpty()) {
            	options.addArguments("window-size=" + config.getBrowserDimension().replace("x", ","));
            }

            if (!config.getBrowserPosition().isEmpty()) {
            	options.addArguments("window-position=" + config.getBrowserPosition().replace("x", ",")); 
            }
        }
    }

    private void addArguments(ChromeOptions options) {
        Map<String, String> settings = getPropertiesStartingWith("argument.");

        for (String key : settings.keySet()) {
            options.addArguments(settings.get(key));
        }
    }

	private void addCapabilities(ChromeOptions options) {
        Map<String, String> settings = getPropertiesStartingWith("capability.");

        for (String key : settings.keySet()) {
            options.setCapability(key, toObject(settings.get(key)));
        }
    }
	
    private void addOptions(ChromeOptions options) {
        Map<String, String> settings = getPropertiesStartingWith("option.");

        for (String key : settings.keySet()) {
            options.setExperimentalOption(key, toObject(settings.get(key)));
        }
    }
    
	private void addPreferences(ChromeOptions options) {
        Map<String, String> settings = getPropertiesStartingWith("preference.");

        Map<String, Object> prefs = new HashMap<>();

        for (String key : settings.keySet()) {
            prefs.put(key, toObject(settings.get(key)));
        }

        if (!prefs.isEmpty()) {
            options.setExperimentalOption("prefs", prefs);
        }
    } 

    private void addExtensions(ChromeOptions options) {
        Map<String, String> settings = getPropertiesStartingWith("extension.");
        String projectPath = new File("").getAbsolutePath();

        for (String key : settings.keySet()) {
            String extension = settings.get(key);

            extension = extension.replace("%PROJECT%", projectPath);

            options.addExtensions(new File(extension));
        }
    }
}
