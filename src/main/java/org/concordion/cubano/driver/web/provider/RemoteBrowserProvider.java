package org.concordion.cubano.driver.web.provider;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.concordion.cubano.driver.web.RemoteHttpClientFactory;
import org.concordion.cubano.utils.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpClient.Factory;

/**
 * Provides everything required to start up a remote browser (desktop or device) - apart from the where to connect.
 * <p>
 * Extend this to provide access to your Selenium grid provider, the framework provides implementations
 * for {@link org.concordion.cubano.driver.web.provider.BrowserStackBrowserProvider BrowserStack}
 * and {@link org.concordion.cubano.driver.web.provider.SauceLabsBrowserProvider SauceLabs}.
 *
 * @author Andrew Sumner
 */
public abstract class RemoteBrowserProvider implements BrowserProvider {
    protected static final String DEFAULT_DESKTOP_SCREENSIZE = "1024x768";
    protected static final String DEFAULT_DESKTOP_VIEWPORT = "950x600";

    private String browser;
    private String viewPort;
    private RemoteType remoteType;
    private DesiredCapabilities capabilites;

    /**
     * Default constructor.
     */
    protected RemoteBrowserProvider() {
    }

    /**
     * Get the session details.
     *
     * @param sessionId Session Id
     * @return Details
     * @throws IOException
     */
    public abstract SessionDetails getSessionDetails(SessionId sessionId) throws IOException;

    /**
     * @return URL to access the remote driver.
     */
    protected abstract String getRemoteDriverUrl();

    /**
     * Constructor.
     *
     * @param remoteType  Type of system the browser is running on
     * @param browser     Name of the browser
     * @param viewPort    Dimensions of the browser
     * @param capabilites Desired capabilities specific to the selenium grid provider
     */
    protected void setDetails(RemoteType remoteType, String browser, String viewPort, DesiredCapabilities capabilites) {
        this.remoteType = remoteType;
        this.browser = browser;
        this.viewPort = viewPort;
        this.capabilites = capabilites;
    }

    @Override
    public String getBrowser() {
        return browser;
    }

    @Override
    public String getViewPort() {
        return viewPort;
    }

    @Override
    public RemoteType getDeviceType() {
        return remoteType;
    }

    @Override
    public String getDeviceName() {
        if (getDeviceType() == RemoteType.DEVICE) {
            return getBrowser();
        } else {
            return "Desktop";
        }
    }

    private DesiredCapabilities getCapabilites() {
        return capabilites;
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebDriver createDriver() {
        URL url;

        try {
            url = new URL(getRemoteDriverUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if (Config.isProxyRequired()) {
            HttpClientBuilder builder = HttpClientBuilder.create();

            HttpHost proxy = new HttpHost(Config.getProxyHost(), Config.getProxyPort());

            CredentialsProvider credsProvider = new BasicCredentialsProvider();

            credsProvider.setCredentials(
                    new AuthScope(Config.getProxyHost(), Config.getProxyPort()),
                    new NTCredentials(Config.getProxyUser(), Config.getProxyPassword(), getWorkstation(), Config.getProxyDomain()));
            if (url.getUserInfo() != null && !url.getUserInfo().isEmpty()) {
                credsProvider.setCredentials(
                        new AuthScope(url.getHost(), (url.getPort() > 0 ? url.getPort() : url.getDefaultPort())),
                        new UsernamePasswordCredentials(url.getUserInfo()));
            }

            builder.setProxy(proxy);
            builder.setDefaultCredentialsProvider(credsProvider);

            Factory factory = new RemoteHttpClientFactory(builder);

            HttpCommandExecutor executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), url, factory);

            return new RemoteWebDriver(executor, getCapabilites());
        } else {
            return new RemoteWebDriver(url, getCapabilites());
        }
    }

    private String getWorkstation() {
        Map<String, String> env = System.getenv();

        if (env.containsKey("COMPUTERNAME")) {
            // Windows
            return env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            // Unix/Linux/MacOS
            return env.get("HOSTNAME");
        } else {
            // From DNS
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                return "Unknown";
            }
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj The object to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    public boolean equals(BrowserProvider obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        RemoteBrowserProvider compare = (RemoteBrowserProvider) obj;

        if (!areEqual(this.getCapabilites(), compare.getCapabilites())) {
            return false;
        }
        if (!areEqual(this.getBrowser(), compare.getBrowser())) {
            return false;
        }
        if (!areEqual(this.getViewPort(), compare.getViewPort())) {
            return false;
        }
        if (!areEqual(this.getDeviceType(), compare.getDeviceType())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.browser, this.capabilites, this.remoteType, this.viewPort);
    }

    /**
     * @return True if is a desktop browser
     */
    public boolean isDesktop() {
        return getDeviceType() == RemoteType.DESKTOP;
    }

    @Override
    public boolean isViewPortDefined() {
        return getViewPort() != null && !getViewPort().isEmpty();
    }

    @Override
    public int getViewPortWidth() {
        if (!isViewPortDefined()) {
            return -1;
        }

        String width = getViewPort().substring(0, getViewPort().indexOf("x")).trim();

        return Integer.parseInt(width);
    }

    @Override
    public int getViewPortHeight() {
        if (!isViewPortDefined()) {
            return -1;
        }

        String height = getViewPort().substring(getViewPort().indexOf("x") + 1).trim();

        return Integer.parseInt(height);
    }

    private boolean areEqual(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        if (obj2 == null) {
            return false;
        }

        return obj1.equals(obj2);
    }
}