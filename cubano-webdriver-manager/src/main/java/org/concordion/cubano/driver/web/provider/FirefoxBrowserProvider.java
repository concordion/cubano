package org.concordion.cubano.driver.web.provider;

import java.io.File;
import java.util.Map;
import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import io.github.bonigarcia.wdm.FirefoxDriverManager;

/**
 * Automatically download, configure and start the WebDriver Manager and browser for Firefox.
 * <p>
 * See https://github.com/bonigarcia/webdrivermanager for details.
 * </p>
 * 
 * @author Andrew Sumner
 */
public class FirefoxBrowserProvider extends LocalBrowserProvider {
    public static final String BROWSER_NAME = "firefox";

    /**
     * For running portable firefox at same time as desktop version:
     * 1. Edit FirefoxPortable.ini (next to FirefoxPortable.exe)
     * 2. If its not there then copy from "Other/Source" folder
     * 3. Change AllowMultipleInstances=false to true
     *
     * @return Starts FireFox driver manager and creates a new WebDriver instance.
     */

    @Override
    public WebDriver createDriver() {
        boolean useLegacyDriver = getPropertyAsBoolean(BROWSER_NAME, "useLegacyDriver", "false");

        if (!useLegacyDriver) {
            // TODO Can we set arguments to try disable the excess logging the marionette driver is making
            setupBrowserManager(FirefoxDriverManager.getInstance());
        }

        FirefoxOptions options = new FirefoxOptions();

        options.setLegacy(useLegacyDriver);

        addProxyCapabilities(options);

        if (!WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME).isEmpty()) {
            options.setBinary(WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME));
        }

        // Profile
        String profileName = getProperty(BROWSER_NAME, "profile", "");
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

            addProfileProperties(profile);
            addExtensions(profile);

            options.setProfile(profile);
        }

                
        addCapabilities(options);
                
        //TODO Are any of these useful?
        //options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.)
        //options.setAcceptInsecureCerts(acceptInsecureCerts)
        //options.addArguments(arguments)
    	//options.setHeadless(headless) ????
        
        stopLogging();
        // I have raised issue https://github.com/mozilla/geckodriver/issues/1016 as this is being ignored
        // options.setLogLevel(FirefoxDriverLogLevel.INFO);
                
        WebDriver driver = new FirefoxDriver(options);

        setBrowserSizeAndLocation(driver);

        return driver;
    }

    private void stopLogging() {
    	//TODO Add config
    	//TODO See if can pass arguments to driver fire FirefoxDriver
    	// https://stackoverflow.com/questions/41387794/how-do-i-disable-firefox-logging-in-selenium-using-geckodriver
        String osNullOutput = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 ? "NUL" : "/dev/null"; 

        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, osNullOutput);
	}

	private void addProfileProperties(FirefoxProfile profile) {
        Map<String, String> properties = getPropertiesStartingWith(BROWSER_NAME, "profile.");

        for (String key : properties.keySet()) {
        	// TODO Do we need to support boolean and integer or happy to set all strings?
            profile.setPreference(key, properties.get(key));
        }
    }
   
    private void addCapabilities(FirefoxOptions options) {
        Map<String, String> properties = getPropertiesStartingWith(BROWSER_NAME, "capability.");

        for (String key : properties.keySet()) {
            options.setCapability(key, toObject(properties.get(key)));
        }
    }

    private void addExtensions(FirefoxProfile profile) {
        Map<String, String> settings = getPropertiesStartingWith(BROWSER_NAME, "extension.");
        String projectPath = new File("").getAbsolutePath();

        for (String key : settings.keySet()) {
            String extension = settings.get(key);

            extension = extension.replace("%PROJECT%", projectPath);

            try {
                profile.addExtension(new File(extension));

                if (extension.contains("firebug")) {
                    String version = new File(extension).getName();
                    version = version.substring(version.indexOf("-") + 1);
                    version = version.substring(0, version.indexOf("-") > 0 ? version.indexOf("-") : version.indexOf("."));

                    profile.setPreference("extensions.firebug.currentVersion", version);
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to add FireFox plugins", e);
            }
        }
    }
}
