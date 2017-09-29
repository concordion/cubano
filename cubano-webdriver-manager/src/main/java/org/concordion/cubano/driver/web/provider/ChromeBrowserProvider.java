package org.concordion.cubano.driver.web.provider;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.ChromeDriverManager;

public class ChromeBrowserProvider extends LocalBrowserProvider {
	public static final String BROWSER_NAME = "chrome";
	
    /**
     * @return Starts Chrome driver manager and creates a new WebDriver instance.
     */
	@Override
    public WebDriver createDriver() {
    	setupBrowserManager(ChromeDriverManager.getInstance());

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();

        addProxyCapabilities(capabilities);

		ChromeOptions options = new ChromeOptions();
		// Workaround for 'Loading of unpacked extensions is disabled by the administrator'
		// https://stackoverflow.com/questions/43797119/failed-to-load-extension-from-popup-box-while-running-selenium-scripts
		options.setExperimentalOption("useAutomationExtension", false);

		// More workarounds https://stackoverflow.com/questions/42979877/chrome-browser-org-openqa-selenium-webdriverexception-unknown-error-cannot-get
		options.addArguments("start-maximized");
		options.addArguments("disable-infobars");
		options.addArguments("--disable-popup-blocking");

        if (!WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME).isEmpty()) {
            options.setBinary(WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME));
        }

		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		WebDriver driver = new ChromeDriver(capabilities);
        
        setBrowserSize(driver);
        
        return driver;        
    }
}
