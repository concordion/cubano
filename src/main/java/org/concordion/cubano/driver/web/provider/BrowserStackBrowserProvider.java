package org.concordion.cubano.driver.web.provider;

import java.io.IOException;

import org.concordion.cubano.driver.http.HttpEasy;
import org.concordion.cubano.driver.http.JsonReader;
import org.concordion.cubano.utils.Config;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;

import com.google.gson.JsonElement;

/**
 * BrowserStack selenium grid provider.
 * <p>
 * <p>Browser and device options: https://www.browserstack.com/automate/java</p>
 */
public class BrowserStackBrowserProvider extends RemoteBrowserProvider {
    private static final String REMOTE_URL = "http://[USER_NAME]:[API_KEY]@hub.browserstack.com/wd/hub";
    private static final String TYPE = "application/json";

    /**
     * Constructor. Uses configuration for browser specified in the configuration file.
     */
    public BrowserStackBrowserProvider() {
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
            case "iphone 6s plus":
                iPhone6SPlusEmulator();
                break;

            case "google nexus 5":
                googleNexus5Emulator();
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
        String url = "https://www.browserstack.com/automate/sessions/" + sessionId + ".json";

        JsonReader json = HttpEasy.request().
                path(url).
                authorization(Config.getRemoteUserName(), Config.getRemoteApiKey()).
                header("Accept", TYPE).header("Content-type", TYPE).
                get().
                getJsonReader();

        SessionDetails details = new SessionDetails();

        details.setProviderName("BrowserStack");

        value = json.jsonPath("automation_session.browser_url");
        details.setBrowserUrl((value == null ? "" : value.getAsString()));

        value = json.jsonPath("automation_session.video_url");
        details.setVideoUrl(value == null ? "" : value.getAsString());

        return details;
    }

    /**
     * Set remote browser details.
     *
     * @param caps Desired capabilities
     * @param browserVersion Browser version
     */
    protected void desktop(DesiredCapabilities caps, String browserVersion) {
        String browserName = caps.getCapability("browser").toString();

        caps.setCapability("browser_version", browserVersion);
        caps.setCapability("resolution", DEFAULT_DESKTOP_SCREENSIZE);

        this.setDetails(RemoteType.DESKTOP, browserName, DEFAULT_DESKTOP_VIEWPORT, caps);
    }

    /**
     * FireFox browser.
     *
     * @param browserVersion Version of the browser
     */
    protected void firefox(String browserVersion) {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browser", "Firefox");
        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");

        this.desktop(caps, browserVersion);
    }

    /**
     * Chrome browser.
     *
     * @param browserVersion Version of the browser
     */
    protected void chrome(String browserVersion) {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browser", "Chrome");
        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");

        this.desktop(caps, browserVersion);
    }

    /**
     * Internet Explorer browser.
     *
     * @param browserVersion Version of the browser
     */
    protected void internetExplorer(String browserVersion) {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browser", "IE");
        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");

        this.desktop(caps, browserVersion);
    }

    /**
     * Safari browser.
     *
     * @param browserVersion Version of the browser
     */
    protected void safari(String browserVersion) {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browser", "Safari");
        caps.setCapability("os", "OS X");
        caps.setCapability("os_version", "El Capitan");

        this.desktop(caps, browserVersion);
    }

    /**
     * Configuration required to start this device on BrowserStack.
     */
    protected void samsungGalaxyS5Emulator() {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browserName", "android");
        caps.setCapability("platform", "ANDROID");
        caps.setCapability("device", "Samsung Galaxy S5");

        this.setDetails(RemoteType.DEVICE, "Samsung Galaxy S5", "1080x1920", caps);
    }

    /**
     * Configuration required to start this device on BrowserStack.
     */
    protected void iPhone6SPlusEmulator() {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browserName", "iPhone");
        caps.setCapability("platform", "MAC");
        caps.setCapability("device", "iPhone 6S Plus");

        this.setDetails(RemoteType.DEVICE, "iPhone 6S Plus", "?x?", caps);
    }

    /**
     * Configuration required to start this device on BrowserStack.
     */
    protected void googleNexus5Emulator() {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browserName", "android");
        caps.setCapability("platform", "ANDROID");
        caps.setCapability("device", "Google Nexus 5");

        this.setDetails(RemoteType.DEVICE, "Google Nexus 5", "1080x1920", caps);
    }
}