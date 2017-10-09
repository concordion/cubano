package org.concordion.cubano.driver.web.provider;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;

import io.github.bonigarcia.wdm.BrowserManager;

/**
 * Base class for local browser providers.
 *
 * @author Andrew Sumner
 */
public abstract class LocalBrowserProvider implements BrowserProvider {

    /**
     * Configures a BrowserManager instance and starts it.
     * 
     * @param instance BrowserManager instance
     */
    protected void setupBrowserManager(BrowserManager instance) {
        if (!WebDriverConfig.getInstance().getProxyAddress().isEmpty()) {
            instance.proxy(WebDriverConfig.getInstance().getProxyAddress());
            instance.proxyUser(WebDriverConfig.getInstance().getProxyUser());
            instance.proxyPass(WebDriverConfig.getInstance().getProxyPassword());
        }

        instance.setup();
    }

    /**
     * Add proxy settings to desired capabilities if specified in config file.
     *
     * @param capabilities Options  
     */
    protected void addProxyCapabilities(MutableCapabilities capabilities) {
        WebDriverConfig config = WebDriverConfig.getInstance();

        if (!config.isProxyRequired()) {
            return;
        }

        String browserProxy = config.getProxyAddress();
        String browserNonProxyHosts = config.getNonProxyHosts();

        final org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
        proxy.setProxyType(org.openqa.selenium.Proxy.ProxyType.MANUAL);
        proxy.setHttpProxy(browserProxy);
        proxy.setFtpProxy(browserProxy);
        proxy.setSslProxy(browserProxy);

        //TODO This was breaking firefox! 
        //proxy.setNoProxy(browserNonProxyHosts);

        capabilities.setCapability(CapabilityType.PROXY, proxy);
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    }

    protected void setBrowserSize(WebDriver driver) {
        if (isBrowserSizeDefined()) {
            driver.manage().window().setSize(new Dimension(getBrowserWidth(), getBrowserHeight()));
        } else if (WebDriverConfig.getInstance().isBrowserMaximized()) {
            driver.manage().window().maximize();
        }
    }

    private boolean isBrowserSizeDefined() {
        return WebDriverConfig.getInstance().getBrowserSize() != null && !WebDriverConfig.getInstance().getBrowserSize().isEmpty();
    }

    private int getBrowserWidth() {
        if (isBrowserSizeDefined()) {
            return -1;
        }

        String width = WebDriverConfig.getInstance().getBrowserSize().substring(0, WebDriverConfig.getInstance().getBrowserSize().indexOf("x")).trim();

        return Integer.parseInt(width);
    }

    private int getBrowserHeight() {
        if (isBrowserSizeDefined()) {
            return -1;
        }

        String height = WebDriverConfig.getInstance().getBrowserSize().substring(WebDriverConfig.getInstance().getBrowserSize().indexOf("x") + 1).trim();

        return Integer.parseInt(height);
    }
}
