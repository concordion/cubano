package org.concordion.cubano.driver.web.provider;

import java.io.File;
import java.util.Map;

import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.MutableCapabilities;
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

    @Override
	protected String getBrowserName() {
		return BROWSER_NAME;
	}
    
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
        boolean useLegacyDriver = getPropertyAsBoolean("useLegacyDriver", "false");

        if (!useLegacyDriver) {
            setupBrowserManager(FirefoxDriverManager.getInstance());
        }

        FirefoxOptions options = new FirefoxOptions();

        options.setLegacy(useLegacyDriver);

        addProxyCapabilities(options);

        if (!getBrowserExe().isEmpty()) {
            options.setBinary(getBrowserExe());
        }

        // Profile
        String profileName = getProperty("profile", "");
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
    	if (getPropertyAsBoolean("disable.logs", "true")) {
        	//TODO Add config
        	//TODO See if can pass arguments to driver fire FirefoxDriver
        	// https://stackoverflow.com/questions/41387794/how-do-i-disable-firefox-logging-in-selenium-using-geckodriver
            String osNullOutput = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 ? "NUL" : "/dev/null"; 

            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, osNullOutput);
    	}
	}

    @Override
    protected void addProxyCapabilities(MutableCapabilities capabilities) {
    	if (getPropertyAsBoolean("useLegacyDriver", "false")) {
    		super.addProxyCapabilities(capabilities);
    	} else {
            // TODO Proxy support only comming with Firefox 57. Cannot test yet as geckodriver win32 0.19.0 not available
            // WebDriverConfig config = WebDriverConfig.getInstance();
            //
            // if (!config.isProxyRequired()) {
            // return;
            // }
            //
            // String proxy = config.getProxyHost();
            // int port = config.getProxyPort();
            // String browserNonProxyHosts = config.getNonProxyHosts();
            //
            // JsonObject json = new JsonObject();
            //
            // json.addProperty("proxyType", "manual");
            // json.addProperty("httpProxy", proxy);
            // json.addProperty("httpProxyPort", port);
            // json.addProperty("ftpProxy", proxy);
            // json.addProperty("ftpProxyPort", port);
            // json.addProperty("sslProxy", proxy);
            // json.addProperty("sslProxyPort", port);
            //
            // capabilities.setCapability(CapabilityType.PROXY, proxy.toString());
            // capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        }
    }
    
	private void addProfileProperties(FirefoxProfile profile) {
        Map<String, String> properties = getPropertiesStartingWith("profile.");

        // Prevent firefox automatically upgrading when running tests
        profile.setPreference("app.update.auto", false);
        profile.setPreference("app.update.enabled", false);
        		 
        for (String key : properties.keySet()) {
            String value = properties.get(key);

            Class<?> valueClass = getClassOfValue(value);

            if (valueClass == Boolean.class) {
                profile.setPreference(key, Boolean.valueOf(value));
            } else if (valueClass == int.class) {
                profile.setPreference(key, Integer.valueOf(value));
            } else {
                profile.setPreference(key, properties.get(key));
            }
        }
    }
   
    private void addCapabilities(FirefoxOptions options) {
        Map<String, String> properties = getPropertiesStartingWith("capability.");

        for (String key : properties.keySet()) {
            options.setCapability(key, toObject(properties.get(key)));
        }
    }

    private void addExtensions(FirefoxProfile profile) {
        Map<String, String> settings = getPropertiesStartingWith("extension.");
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
