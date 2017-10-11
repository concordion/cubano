package org.concordion.cubano.driver.web.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.ChromeDriverManager;

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

    /**
     * @return Starts Chrome driver manager and creates a new WebDriver instance.
     */
    @Override
    public WebDriver createDriver() {
        setupBrowserManager(ChromeDriverManager.getInstance());

        ChromeOptions options = new ChromeOptions();

        addProxyCapabilities(options);

        if (WebDriverConfig.getInstance().isBrowserMaximized()) {
            options.addArguments("start-maximized");
        }

        // Workaround for 'Loading of unpacked extensions is disabled by the administrator'
        // https://stackoverflow.com/questions/43797119/failed-to-load-extension-from-popup-box-while-running-selenium-scripts
        // options.setExperimentalOption("useAutomationExtension", false);

        // // More workarounds https://stackoverflow.com/questions/42979877/chrome-browser-org-openqa-selenium-webdriverexception-unknown-error-cannot-get
        // options.addArguments("disable-infobars");
        // options.addArguments("--disable-popup-blocking");

        if (!WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME).isEmpty()) {
            options.setBinary(WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME));
        }

        addCapabilities(options);
        addArguments(options);
        addOptions(options);
        addPreferences(options);
        addExtensions(options);

        WebDriver driver = new ChromeDriver(options);

        // TODO Why does this error?
        // setBrowserSize(driver);

        return driver;
    }

    private void addCapabilities(ChromeOptions options) {
        Map<String, String> settings = WebDriverConfig.getInstance().getPropertiesStartingWith("chrome.capability.", true);

        for (String key : settings.keySet()) {
            options.setCapability(key, settings.get(key));
        }
    }

    private void addArguments(ChromeOptions options) {
        Map<String, String> settings = WebDriverConfig.getInstance().getPropertiesStartingWith("chrome.argument.", true);

        options.addArguments("test-type");

        for (String key : settings.keySet()) {
            options.addArguments(settings.get(key));
        }
    }

    private void addOptions(ChromeOptions options) {
        Map<String, String> settings = WebDriverConfig.getInstance().getPropertiesStartingWith("chrome.option.", true);

        for (String key : settings.keySet()) {
            options.setExperimentalOption(key, settings.get(key));
        }
    }

    private void addPreferences(ChromeOptions options) {
        Map<String, String> settings = WebDriverConfig.getInstance().getPropertiesStartingWith("chrome.preference.", true);

        Map<String, Object> prefs = new HashMap<>();

        for (String key : settings.keySet()) {
            prefs.put(key, settings.get(key));
        }

        if (!prefs.isEmpty()) {
            options.setExperimentalOption("prefs", prefs);
        }
    } 

    private void addExtensions(ChromeOptions options) {
        Map<String, String> settings = WebDriverConfig.getInstance().getPropertiesStartingWith("chrome.extension.", true);
        String projectPath = new File("").getAbsolutePath();

        for (String key : settings.keySet()) {
            String extension = settings.get(key);

            extension = extension.replace("%PROJECT%", projectPath);

            options.addExtensions(new File(extension));
        }
    }
}
