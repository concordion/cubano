package org.concordion.cubano.driver.web.provider;

import java.io.IOException;

import org.concordion.cubano.driver.http.HttpEasy;
import org.concordion.cubano.driver.http.JsonReader;
import org.concordion.cubano.utils.Config;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;

import com.google.gson.JsonElement;

/**
 * SauceLabs selenium grid provider.
 * <p>
 * <p>Browser and device options: https://wiki.saucelabs.com/display/DOCS/Platform+Configurator</p>
 */
public class SauceLabsBrowserProvider extends RemoteBrowserProvider {
    private static final String REMOTE_URL = "http://[USER_NAME]:[API_KEY]@ondemand.saucelabs.com:80/wd/hub";
    private static final String TYPE = "application/json";

    /**
     * Constructor. Uses configuration for browser specified in the configuration file.
     */
    public SauceLabsBrowserProvider() {
        String browser = Config.getBrowser();

        if (browser == null) {
            browser = "";
        }

        // check desktop browsers
        String[] browserDetails = browser.split(" ");

        if (browserDetails.length <= 2) {
            String version = "";

            if (browserDetails.length == 2) {
                version = browserDetails[1];
            }

            switch (browserDetails[0]) {
                case "chrome":
                    chrome(version);
                    break;

                case "internetExplorer":
                case "ie":
                    internetExplorer(version);
                    break;

                case "firefox":
                    firefox(version);
                    break;

                case "safari":
                    safari(version);
                    break;

                default:
                    break;
            }
        }


        // check devices
        switch (browser) {
            case "iPhone6":
                iPhone6();
                break;

            case "iPhone6PlusEmulator":
                iPhone6PlusEmulator();
                break;

            case "samsungGalaxyS5":
                samsungGalaxyS5();
                break;

            case "samsungGalaxyS4Emulator":
                samsungGalaxyS4Emulator();
                break;

            case "googleNexus7CEmulator":
                googleNexus7CEmulator();
                break;


            default:
                break;
        }

        throw new RuntimeException("Browser '" + browser + "' is not currently supported");
    }

    @Override
    protected String getRemoteDriverUrl() {
        return REMOTE_URL.replace("[USER_NAME]", Config.getRemoteUserName()).replace("[API_KEY]", Config.getRemoteApiKey());
    }

    @Override
    public SessionDetails getSessionDetails(SessionId sessionId) throws IOException {
        JsonElement value;
        String url = "https://saucelabs.com/rest/v1/" + Config.getRemoteUserName() + "/jobs/" + sessionId;

        JsonReader reader = HttpEasy.request()
                .path(url)
                .authorization(Config.getRemoteUserName(), Config.getRemoteApiKey())
                .header("Accept", TYPE).header("Content-type", TYPE)
                .get()
                .getJsonReader();

        SessionDetails details = new SessionDetails();

        details.setProviderName("SauceLabs");

        //TODO - remove beta when this interface becomes the default
        details.setBrowserUrl("https://www.saucelabs.com/beta/tests/" + sessionId);

        value = reader.jsonPath("video_url");
        details.setVideoUrl((value == null ? "" : value.getAsString()));

        return details;
    }

    /**
     * Register a desktop browser.
     *
     * @param caps Desired capabilities
     * @param browserName Type of browser
     * @param browserVersion Version of the browser
     */
    protected void desktop(DesiredCapabilities caps, String browserName, String browserVersion) {
        String platform = caps.getCapability(CapabilityType.PLATFORM).toString();

        caps.setCapability("version", browserVersion);
        caps.setCapability("screenResolution", DEFAULT_DESKTOP_SCREENSIZE);
        caps.setCapability("name", String.format("%s %s, %s", browserName, browserVersion, platform));

        this.setDetails(RemoteType.DESKTOP, browserName, DEFAULT_DESKTOP_VIEWPORT, caps);
    }

    /**
     * Register a desktop browser.
     *
     * @param caps Desired capabilities
     * @param browserVersion Version of the browser
     */
    protected void desktop(DesiredCapabilities caps, String browserVersion) {
        String browserName = caps.getCapability(CapabilityType.BROWSER_NAME).toString();

        desktop(caps, browserName, browserVersion);
    }

    /**
     * FireFox browser.
     *
     * @param browserVersion Version of the browser
     */
    protected void firefox(String browserVersion) {
        DesiredCapabilities caps = DesiredCapabilities.firefox();
        caps.setCapability("platform", "Windows 10");

        desktop(caps, browserVersion);
    }

    /**
     * Chrome browser.
     *
     * @param browserVersion Version of the browser
     */
    protected void chrome(String browserVersion) {
        DesiredCapabilities caps = DesiredCapabilities.chrome();
        caps.setCapability("platform", "Windows 10");

        desktop(caps, browserVersion);
    }

    /**
     * Internet Explorer browser.
     *
     * @param browserVersion Version of the browser
     */
    protected void internetExplorer(String browserVersion) {
        DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
        caps.setCapability("platform", "Windows 10");

        desktop(caps, "ie", browserVersion);
    }

    /**
     * Safari browser.
     *
     * @param browserVersion Version of the browser
     */
    protected void safari(String browserVersion) {
        DesiredCapabilities caps = DesiredCapabilities.safari();
        caps.setCapability("platform", "OS X 10.11");

        desktop(caps, browserVersion);
    }

    /**
     * Configuration required to start this device.
     */
    protected void googleNexus7CEmulator() {
        DesiredCapabilities caps = DesiredCapabilities.android();
        caps.setCapability("deviceName", "Google Nexus 7C Emulator");
        caps.setCapability("deviceOrientation", "portrait");
        caps.setCapability("name", "Google Nexus 7C Emulator");

        this.setDetails(RemoteType.DEVICE, "Google Nexus 7C Emulator", "?x?", caps);
    }

    /**
     * Configuration required to start this device.
     */
    protected void samsungGalaxyS4Emulator() {
        DesiredCapabilities caps = DesiredCapabilities.android();
        caps.setCapability("deviceName", "Samsung Galaxy S4 Emulator");
        caps.setCapability("deviceOrientation", "portrait");
        caps.setCapability("name", "Samsung Galaxy S4 Emulator");

        this.setDetails(RemoteType.DEVICE, "Samsung Galaxy S4 Emulator", "?x?", caps);
    }

    /**
     * Configuration required to start this device.
     */
    protected void samsungGalaxyS5() {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("deviceName", "Samsung Galaxy S5 Device");
        caps.setCapability("platformName", "Android");
        caps.setCapability("platformVersion", "4.4");
        caps.setCapability("browserName", "Chrome");
        caps.setCapability("name", "Samsung Galaxy S5");

        this.setDetails(RemoteType.DEVICE, "Samsung Galaxy S5", "?x?", caps);
    }

    /**
     * Configuration required to start this device.
     */
    protected void iPhone6PlusEmulator() {
        DesiredCapabilities caps = DesiredCapabilities.iphone();
        caps.setCapability("platform", "OS X 10.10");
        caps.setCapability("version", "9.2");
        caps.setCapability("deviceName", "iPhone 6 Plus");
        caps.setCapability("deviceOrientation", "portrait");
        caps.setCapability("name", "iPhone 6 Plus");

        this.setDetails(RemoteType.DEVICE, "iPhone 6 Plus", "?x?", caps);
    }

    /**
     * Configuration required to start this device.
     */
    protected void iPhone6() {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("deviceName", "iPhone 6 Device");
        caps.setCapability("platformName", "iOS");
        caps.setCapability("platformVersion", "8.0");
        caps.setCapability("browserName", "Safari");
        caps.setCapability("name", "iPhone 6");

        this.setDetails(RemoteType.DEVICE, "iPhone 6", "?x?", caps);
    }
}