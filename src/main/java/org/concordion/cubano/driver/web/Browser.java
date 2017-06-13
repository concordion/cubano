package org.concordion.cubano.driver.web;

import org.concordion.cubano.driver.web.pagefactory.PageObjectAwareHtmlElementsLoader;
import org.concordion.cubano.driver.web.provider.BrowserProvider;
import org.concordion.cubano.driver.web.provider.RemoteBrowserProvider;
import org.concordion.cubano.driver.web.provider.SessionDetails;
import org.concordion.cubano.utils.Config;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A wrapper around Selenium WebDriver to make it easier to open and close a
 * specific browser regardless of whether that browser is running locally or
 * remotely.
 *
 * @author Andrew Sumner
 */
public class Browser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Browser.class);
    private WebDriver wrappedDriver = null;
    private EventFiringWebDriver eventFiringDriver = null;
    private SeleniumEventLogger eventListener;
    private SessionId sessionId = null;
    private BrowserProvider browserConfig;

    /**
     * Constructor - does not start the browser.
     */
    public Browser() {
    }

    /**
     * Get the session details from the Selenium grid provider, if not running
     * on a Selenium grid then returns null.
     *
     * @return Sessiond details if running on selenium grid, otherwise null
     */
    public SessionDetails getSessionDetails() {
        if (sessionId == null) {
            return null;
        }

        try {
            return ((RemoteBrowserProvider) browserConfig).getSessionDetails(sessionId);
        } catch (IOException e) {
            throw new RuntimeException("Error while getting session details from selenium grid provider", e);
        }
    }

    /**
     * Are we running on selenium grid?
     *
     * @return true if browser is running on selenium grid, false if running
     * locally
     */
    public boolean isRemoteDriver() {
        return this.browserConfig instanceof RemoteBrowserProvider;
    }

    /**
     * Is the browser open?
     *
     * @return true or false
     */
    public boolean isOpen() {
        return this.wrappedDriver != null;
    }

    /**
     * The WebDriver is wrapped inside an EventFiringWebDriver to provide
     * detailed logging, this will return the underlying WebDriver in the event
     * it is required.
     *
     * @return Original WebDriver object
     */
    public WebDriver getWrappedDriver() {
        return this.wrappedDriver;
    }

    /**
     * Provide access to the WebDriver object.
     *
     * @return WebDriver
     */
    public WebDriver getDriver() {
        if (!isOpen()) {
            this.open();
        }

        registerScreenshotTaker();

        return this.eventFiringDriver;
    }

    /**
     * Allows wrapping the the base driver supplied by
     * {@link #getWrappedDriver()} in an additional layer - such as that used by
     * Applitools-Eyes for visual regression checking.
     *
     * @param driver New driver
     */
    public void setDriver(WebDriver driver) {
        this.eventFiringDriver.unregister(this.eventListener);
        this.eventFiringDriver = new EventFiringWebDriver(driver);
        this.eventFiringDriver.register(this.eventListener);
    }


    /**
     * Provides an HtmlElementsLoader that provides findElement(s) methods for HtmlElement based classes.
     *
     * @param pageObject PageObject sitting on
     * @return HtmlElementsLoader
     */
    public PageObjectAwareHtmlElementsLoader getHtmlElementsLoader(BasePageObject<?> pageObject) {
        return new PageObjectAwareHtmlElementsLoader(eventFiringDriver, pageObject);
    }

    /**
     * Opens a browser obtaining browser settings from configuration file.
     *
     * @return WebDriver
     */
    public WebDriver open() {
        if (this.browserConfig == null) {
            this.browserConfig = Browser.getConfiguredBrowser();
        }

        return open(this.browserConfig);
    }

    /**
     * Opens a browser using supplied configuration.
     *
     * @param config Browser definition
     * @return WebDriver
     */
    public WebDriver open(BrowserProvider config) {
        if (this.eventFiringDriver != null) {
            throw new RuntimeException("Browser is already open");
        }

        LOGGER.debug("Starting browser");

        this.browserConfig = config;

        this.wrappedDriver = config.createDriver();
        this.eventFiringDriver = new EventFiringWebDriver(this.wrappedDriver);
        this.eventListener = new SeleniumEventLogger();
        this.eventFiringDriver.register(this.eventListener);

        if (isRemoteDriver()) {
            this.sessionId = ((RemoteWebDriver) getWrappedDriver()).getSessionId();
        } else {
            this.sessionId = null;
        }

        return this.eventFiringDriver;
    }

    /**
     * Close current browser.
     */
    public void close() {
        if (this.wrappedDriver == null) {
            return;
        }

        LOGGER.debug("Closing browser");
        removeScreenshotTaker();

        try {
            this.eventFiringDriver.unregister(this.eventListener);
            this.eventFiringDriver.quit();
        } catch (Exception ex) {
            LOGGER.warn("Exception attempting to quit the browser: " + ex.getMessage());
        }

        this.eventFiringDriver = null;
        this.wrappedDriver = null;
    }

    /**
     * @return The current browser configuration
     */
    public BrowserProvider getConfiguration() {
        return this.browserConfig;
    }

    /**
     * Provide the configuration for the browser as selected in the
     * configuration file.
     *
     * @return Browser configuration
     */
    public static BrowserProvider getConfiguredBrowser() {
        try {
            return (BrowserProvider) Class.forName(Config.getBrowserProvider()).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Unable to create class " + Config.getBrowserProvider(), e);
        }

    }

    /**
     * Register the Screenshot Taker.
     */
    public void registerScreenshotTaker() {
        if (!ReportLoggerFactory.hasScreenshotTaker()) {
            ReportLoggerFactory.setScreenshotTaker(new SeleniumScreenshotTaker(wrappedDriver));
        }
    }

    /**
     * Remove the Screenshot Taker.
     */
    public void removeScreenshotTaker() {
        ReportLoggerFactory.removeScreenshotTaker();
    }
}
