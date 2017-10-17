package org.concordion.cubano.driver.web.provider;

import java.util.Map;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;

import io.github.bonigarcia.wdm.BrowserManager;

/**
 * Base class for local browser providers.
 *
 * @author Andrew Sumner
 */
public abstract class LocalBrowserProvider implements BrowserProvider {
    private WebDriverConfig config = WebDriverConfig.getInstance();

    /**
     * Configures a BrowserManager instance and starts it.
     * 
     * @param instance BrowserManager instance
     */
    protected void setupBrowserManager(BrowserManager instance) {
        if (!config.getProxyAddress().isEmpty()) {
            instance.proxy(config.getProxyAddress());
            instance.proxyUser(config.getProxyUser());
            instance.proxyPass(config.getProxyPassword());
        }

        instance.setup();
    }

    /**
     * Add proxy settings to desired capabilities if specified in config file.
     *
     * @param capabilities Options  
     */
    protected void addProxyCapabilities(MutableCapabilities capabilities) {
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

        if (!browserNonProxyHosts.isEmpty()) {
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

    protected String getProperty(String browser, String key, String defaultValue) {
        return config.getProperty(browser + "." + key, defaultValue);
    }

    protected boolean getPropertyAsBoolean(String browser, String key, String defaultValue) {
        return config.getPropertyAsBoolean(browser + "." + key, defaultValue);
    }

    protected int getPropertyAsInteger(String browser, String key, String defaultValue) {
        return config.getPropertyAsInteger(browser + "." + key, defaultValue);
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

    protected  Map<String, String> getPropertiesStartingWith(String browser, String key) {
        return config.getPropertiesStartingWith(browser + "." + key, true);
    }
    
}
