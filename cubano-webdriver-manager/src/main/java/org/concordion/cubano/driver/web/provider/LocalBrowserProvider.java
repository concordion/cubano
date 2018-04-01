package org.concordion.cubano.driver.web.provider;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.concordion.cubano.config.Config;
import org.concordion.cubano.config.PropertyLoader;
import org.concordion.cubano.config.ProxyConfig;
import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Base class for local browser providers.
 *
 * @author Andrew Sumner
 */
public abstract class LocalBrowserProvider implements BrowserProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalBrowserProvider.class);

    private PropertyLoader propertyLoader = Config.getInstance().getPropertyLoader();
    private ProxyConfig proxyConfig = Config.getInstance().getProxyConfig();
    private WebDriverConfig config = WebDriverConfig.getInstance();
    protected String driverPath = null;

    /**
     * 
     * @return The name of the browser as used in the configuration file to retrieve browser specific settings.
     */
    protected abstract String getBrowserName();

    /**
     * Configures a BrowserManager instance and starts it.
     * 
     * @param instance BrowserManager instance
     */
    protected void setupBrowserManager(WebDriverManager instance) {
        // Make all WebDriverManager properties in configuration file system properties
        Map<String, String> result = propertyLoader.getPropertiesStartingWith("wdm.");
        Map<String, String> override = propertyLoader.getPropertiesStartingWith(getBrowserName() + ".wdm.");

        for (String key : override.keySet()) {
            result.put(key.substring(getBrowserName().length() + 1), override.get(key));
        }

        for (String key : result.keySet()) {
            String value = result.get(key);

            // TODO Should we avoid system properties for any other settings or just this one?
            switch (key.toLowerCase()) {
            case "wdm.architecture":
                instance.architecture(Architecture.valueOf(value));
                break;

            case "wdm.checkforupdates":
                CheckForUpdates check = CheckForUpdates.valueOf(value.toUpperCase());

                if (check != CheckForUpdates.ALWAYS) {
                    Preferences prefs = Preferences.userNodeForPackage(LocalBrowserProvider.class);
                    String prefKey = getBrowserName() + ".last_checked_time";
                    Date lastChecked = new Date(prefs.getLong(prefKey, new Date(0L).getTime()));

                    if (check.recheckIsRequired(lastChecked)) {
                        prefs.putLong(prefKey, new Date().getTime());
                        try {
                            prefs.flush();
                        } catch (BackingStoreException e) {
                            throw new RuntimeException("Unable to update last checked date", e);
                        }
                    } else {
                        instance.forceCache();
                    }
                }
                break;

            default:
                System.setProperty(key, value);
            }
        }

        if (!proxyConfig.getProxyAddress().isEmpty()) {
            instance.proxy(proxyConfig.getProxyAddress());
            instance.proxyUser(proxyConfig.getProxyUsername());
            instance.proxyPass(proxyConfig.getProxyPassword());
        }

        instance.setup();

        driverPath = instance.getBinaryPath();
    }

    /**
     * Useful if local browser is not available on path.
     * 
     * @return Path to browser executable
     */
    public String getBrowserExe() {
        String localBrowserExe = propertyLoader.getProperty(getBrowserName() + ".exe", null);

        if (!localBrowserExe.isEmpty()) {
            return localBrowserExe.replace("%USERPROFILE%", System.getProperty("USERPROFILE", ""));
        }

        return "";
    }

    /**
     * Add proxy settings to desired capabilities if specified in config file.
     *
     * @param capabilities Options
     */
    protected void addProxyCapabilities(MutableCapabilities capabilities) {
        if (!proxyConfig.isProxyRequired()) {
            return;
        }

        String browserProxy = proxyConfig.getProxyAddress();
        String browserNonProxyHosts = proxyConfig.getNonProxyHosts();

        final org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();

        proxy.setProxyType(org.openqa.selenium.Proxy.ProxyType.MANUAL);
        proxy.setHttpProxy(browserProxy);
        proxy.setFtpProxy(browserProxy);
        proxy.setSslProxy(browserProxy);

        if (browserNonProxyHosts != null && !browserNonProxyHosts.isEmpty()) {
            // proxy.setNoProxy - defines a String, but expects an array (as per
            // https://w3c.github.io/webdriver/webdriver-spec.html#proxy)
            // BUG raised at - https://github.com/mozilla/geckodriver/issues/1164
            // Workaround defined at - https://github.com/SeleniumHQ/selenium/issues/5004
            proxy.setNoProxy(browserNonProxyHosts);
        }

        capabilities.setCapability(CapabilityType.PROXY, proxy);

        // TODO This should probably be configurable
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    }

    protected void setBrowserSizeAndLocation(WebDriver driver) {
        if (config.isBrowserMaximized()) {
            driver.manage().window().maximize();
        } else {
            if (!config.getBrowserDimension().isEmpty()) {
                driver.manage().window().setSize(getBrowserDimension());
            }

            if (!config.getBrowserPosition().isEmpty()) {
                driver.manage().window().setPosition(getBrowserPosition());
            }
        }
    }

    private Dimension getBrowserDimension() {
        String width = config.getBrowserDimension().substring(0, config.getBrowserDimension().indexOf("x")).trim();
        String height = config.getBrowserDimension().substring(config.getBrowserDimension().indexOf("x") + 1).trim();

        return new Dimension(Integer.parseInt(width), Integer.parseInt(height));
    }

    private Point getBrowserPosition() {
        String x = config.getBrowserPosition().substring(0, config.getBrowserPosition().indexOf("x")).trim();
        String y = config.getBrowserPosition().substring(config.getBrowserPosition().indexOf("x") + 1).trim();

        return new Point(Integer.parseInt(x), Integer.parseInt(y));
    }

    protected String getProperty(String key, String defaultValue) {
        return propertyLoader.getProperty(getBrowserName() + "." + key, defaultValue);
    }

    protected boolean getPropertyAsBoolean(String key, String defaultValue) {
        return propertyLoader.getPropertyAsBoolean(getBrowserName() + "." + key, defaultValue);
    }

    protected int getPropertyAsInteger(String key, String defaultValue) {
        return propertyLoader.getPropertyAsInteger(getBrowserName() + "." + key, defaultValue);
    }

    protected Map<String, String> getPropertiesStartingWith(String key) {
        return propertyLoader.getPropertiesStartingWith(getBrowserName() + "." + key, true);
    }

    protected Object toObject(String value) {
        Class<?> valueClass = getClassOfValue(value);

        if (valueClass == null) {
            return null;
        }

        if (valueClass == Boolean.class) {
            return Boolean.valueOf(value);
        }

        if (valueClass == int.class) {
            return Integer.valueOf(value);
        }

        return value;
    }

    protected Class<?> getClassOfValue(String value) {
        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.class;
        }

        if (value.matches("^-?\\d+$")) {
            return int.class;
        }

        return String.class;
    }

    public void cleanup() {
        if (driverPath != null) {
            boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

            if (isDebug) {
                try {
                    String cmd;
                    boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;

                    if (isWindows) {
                        cmd = String.format("taskkill /F /IM %s", new File(driverPath).getName());
                    } else {
                        cmd = String.format("pkill -f \"%s\"", new File(driverPath).getName());
                    }

                    LOGGER.debug("Cleaning up any orphaned browser drivers using command: {}", cmd);
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException e) {
                    LOGGER.warn("Unable to terminate browser driver", e);
                }
            }
        }
    }
}
