package org.concordion.cubano.driver.web.provider;

import java.io.File;
import java.util.Map;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
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
        boolean useLegacyDriver = WebDriverConfig.getInstance().getPropertyAsBoolean(BROWSER_NAME + ".useLegacyDriver", "false");

        if (!useLegacyDriver) {
            // TODO Can we set arguments to try disable the excess logging the marionette driver is making
            setupBrowserManager(FirefoxDriverManager.getInstance());
        }

        FirefoxOptions options = new FirefoxOptions();

        options.setLogLevel(FirefoxDriverLogLevel.INFO);
        options.setLegacy(useLegacyDriver);

        addProxyCapabilities(options);

        if (!WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME).isEmpty()) {
            options.setBinary(WebDriverConfig.getInstance().getBrowserExe(BROWSER_NAME));
        }

        // Profile
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

            addProfileProperties(profile);
            addExtensions(profile);

            options.setProfile(profile);
        }

        addCapabilities(options);

        WebDriver driver = new FirefoxDriver(options);

        setBrowserSize(driver);

        return driver;
    }

    private void addProfileProperties(FirefoxProfile profile) {
        Map<String, String> properties = WebDriverConfig.getInstance().getPropertiesStartingWith("firefox.profile.", true);

        for (String key : properties.keySet()) {
            profile.setPreference(key, properties.get(key));
        }
    }

    private void addCapabilities(FirefoxOptions options) {
        Map<String, String> properties = WebDriverConfig.getInstance().getPropertiesStartingWith("firefox.capability.", true);

        for (String key : properties.keySet()) {
            options.setCapability(key, properties.get(key));
        }
    }

    private void addExtensions(FirefoxProfile profile) {
        Map<String, String> settings = WebDriverConfig.getInstance().getPropertiesStartingWith("firefox.extension.", true);
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
