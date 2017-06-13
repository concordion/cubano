package org.concordion.cubano.driver.web.provider;

import java.io.File;
import java.io.FilenameFilter;

import org.concordion.cubano.utils.Config;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;

/**
 * Provides everything required to start up a local desktop browser, currently supports chrome, ie and firefox
 * <p>
 * Browser drivers are automatically downloaded as required when requesting a browser: see https://github.com/bonigarcia/webdrivermanager for details
 * TODO: How configure proxy, download location and other customisation
 *
 * @author Andrew Sumner
 */
public class LocalBrowserProvider implements BrowserProvider {
    private String browser;
    private String browserSize;
    private boolean maximised;

    /**
     * Reads in configuration from the configuration file.
     */
    public LocalBrowserProvider() {
        browser = Config.getBrowser();
        browserSize = Config.getBrowserSize();
        maximised = true;
    }

    /**
     * @return A new Selenium WebDriver based on supplied configuration
     */
    @Override
    public WebDriver createDriver() {
        WebDriver driver;

        // use web driver as specified in config.properties
        switch (browser.toLowerCase()) {
            case "chrome":
                driver = createChromeDriver();
                break;

            case "edge":
                driver = createEdgeDriver();
                break;

            case "firefox":
                driver = createFireFoxDriver();
                break;

            case "ie":
            case "internetexplorer":
                driver = createInternetExplorerDriver();

                break;

            case "opera":
                driver = createOperaDriver();
                break;

            case "phantomjs":
                driver = createPhantomJsDriver();
                break;

            default:
                throw new RuntimeException("Browser '" + browser + "' is not currently supported");
        }

        if (isViewPortDefined()) {
            driver.manage().window().setSize(new Dimension(getViewPortWidth(), getViewPortHeight()));
        } else if (maximised) {
            driver.manage().window().maximize();
        }

        return driver;
    }

    /**
     * @return Starts Chrome driver manager and creates a new WebDriver instance.
     */
    protected WebDriver createChromeDriver() {
        ChromeDriverManager.getInstance().setup();

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();

        addProxyCapabilities(capabilities);

        if (!Config.getBrowserExe().isEmpty()) {
            ChromeOptions options = new ChromeOptions();
            options.setBinary(Config.getBrowserExe());
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        }

        return new ChromeDriver(capabilities);
    }

    /**
     * @return Starts Edge driver manager and creates a new WebDriver instance.
     */
    protected WebDriver createEdgeDriver() {
        EdgeDriverManager.getInstance().setup();

        DesiredCapabilities capabilities = DesiredCapabilities.edge();

        addProxyCapabilities(capabilities);

        return new EdgeDriver(capabilities);
    }

    /**
     * For running portable firefox at same time as desktop version:
     *      1. Edit FirefoxPortable.ini (next to FirefoxPortable.exe)
     *      2. If its not there then copy from "Other/Source" folder
     *      3. Change AllowMultipleInstances=false to true
     *
     * @return Starts FireFox driver manager and creates a new WebDriver instance.
     */
    protected WebDriver createFireFoxDriver() {
        FirefoxDriverManager.getInstance().setup();

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();

        addProxyCapabilities(capabilities);

        if (!Config.getBrowserExe().isEmpty()) {
            // TODO Should we do this way?
//            FirefoxOptions options = new FirefoxOptions();
//            options.setBinary(Config.getBrowserExe());
//            capabilities.setCapability(FirefoxDriver.CAPABILITY, options);

            capabilities.setCapability(FirefoxDriver.BINARY, Config.getBrowserExe());
        }

        if (Config.activatePlugins()) {
            FirefoxProfile profile = new FirefoxProfile();

            try {
                File firebug = Plugins.get("firebug");
                profile.addExtension(firebug);

                String version = firebug.getName();
                version = version.substring(version.indexOf("-") + 1);
                version = version.substring(0, version.indexOf("-") > 0 ? version.indexOf("-") : version.indexOf("."));

                profile.setPreference("extensions.firebug.currentVersion", version);

                profile.addExtension(Plugins.get("firepath"));
            } catch (Exception e) {
                throw new RuntimeException("Unable to add FireFox plugins", e);
            }

            capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        }

        return new FirefoxDriver(capabilities);
    }

    /**
     * @return Starts Internet Explorer driver manager and creates a new WebDriver instance.
     */
    protected WebDriver createInternetExplorerDriver() {
        InternetExplorerDriverManager.getInstance().setup();

        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();

        // capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        // "ignore", "accept", or "dismiss".
        // capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "dismiss");

        // capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

        addProxyCapabilities(capabilities);

        return new InternetExplorerDriver(capabilities);
    }

    /**
     * @return Starts Opera driver manager and creates a new WebDriver instance.
     */
    protected WebDriver createOperaDriver() {
        OperaDriverManager.getInstance().setup();

        DesiredCapabilities capabilities = DesiredCapabilities.operaBlink();

        addProxyCapabilities(capabilities);

        if (!Config.getBrowserExe().isEmpty()) {
            OperaOptions options = new OperaOptions();
            options.setBinary(Config.getBrowserExe());
            capabilities.setCapability(OperaOptions.CAPABILITY, options);
        }

        return new OperaDriver(capabilities);
    }

    /**
     * @return Starts PhantomJs driver manager and creates a new WebDriver instance.
     */
    protected WebDriver createPhantomJsDriver() {
        PhantomJsDriverManager.getInstance().setup();

        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();

        addProxyCapabilities(capabilities);

        return new PhantomJSDriver(capabilities);
    }

    /**
     * Add proxy settings to desired capabilities if specified in config file.
     *
     * @param capabilities Desired capabilities
     */
    protected void addProxyCapabilities(DesiredCapabilities capabilities) {
        if (!Config.isProxyRequired()) {
            return;
        }

        String browserProxy = Config.getProxyHost() + ":" + Config.getProxyPort();
        String browserNoProxyList = Config.getNoProxyList();

        final org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
        proxy.setProxyType(org.openqa.selenium.Proxy.ProxyType.MANUAL);
        proxy.setHttpProxy(browserProxy);
        proxy.setFtpProxy(browserProxy);
        proxy.setSslProxy(browserProxy);
        proxy.setNoProxy(browserNoProxyList);

        capabilities.setCapability(CapabilityType.PROXY, proxy);
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    }


    public boolean isViewPortDefined() {
        return browserSize != null && !browserSize.isEmpty();
    }


    @Override
    public String getViewPort() {
        return browserSize;
    }

    @Override
    public int getViewPortWidth() {
        if (browserSize == null || browserSize.isEmpty()) {
            return -1;
        }

        String width = browserSize.substring(0, browserSize.indexOf("x")).trim();

        return Integer.parseInt(width);
    }

    @Override
    public int getViewPortHeight() {
        if (browserSize == null || browserSize.isEmpty()) {
            return -1;
        }

        String height = browserSize.substring(browserSize.indexOf("x") + 1).trim();

        return Integer.parseInt(height);
    }


    @Override
    public RemoteType getDeviceType() {
        return RemoteType.DESKTOP;
    }

    @Override
    public String getDeviceName() {
        return "Desktop";
    }

    @Override
    public String getBrowser() {
        return browser;
    }

    /**
     * Helper for finding Browser plug-ins stored in the libs folder..
     */
    private static class Plugins {
        public static File get(final String pluginName) {
            File search = new File("libs");

            String[] files = search.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.contains(pluginName);
                }
            });

            if (files != null && files.length > 0) {
                return new File(search, files[0]);
            }

            return null;
        }
    }
}
