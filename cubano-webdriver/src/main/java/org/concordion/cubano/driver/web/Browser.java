package org.concordion.cubano.driver.web;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;

import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.concordion.cubano.driver.web.pagefactory.PageObjectAwareHtmlElementsLoader;
import org.concordion.cubano.driver.web.provider.BrowserProvider;
import org.concordion.cubano.driver.web.provider.RemoteBrowserProvider;
import org.concordion.ext.ScreenshotTaker;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around Selenium WebDriver to make it easier to open and close a
 * specific browser regardless of whether that browser is running locally or
 * remotely.
 *
 * @author Andrew Sumner
 */
public class Browser implements Closeable {
    public static final String DEFAULT = "DEFAULT";

    private static final Logger LOGGER = LoggerFactory.getLogger(Browser.class);
    private WebDriver wrappedDriver = null;
    private EventFiringWebDriver eventFiringDriver = null;
    private SeleniumEventLogger eventListener = null;
    private SessionId sessionId = null;
    private BrowserProvider browserProvider = null;
    private static Class<? extends ScreenshotTaker> screenshotTaker = SeleniumScreenshotTaker.class;

    /**
     * Constructor - does not start the browser.
     */
    public Browser() {
    }

    /**
     * Constructor - does not start the browser.
     * @param browserProvider Information required to start a browser locally or remotely
     */
    public Browser(BrowserProvider browserProvider) {
        this.browserProvider = browserProvider;
    }

    /**
     * Are we running on selenium grid?
     *
     * @return true if browser is running on selenium grid, false if running
     *         locally
     */
    public boolean isRemoteDriver() {
        return this.browserProvider instanceof RemoteBrowserProvider;
    }

    /**
     * Is the browser open?
     *
     * @return true or false
     */
    public boolean isOpen() {
        return getActiveDriver() != null;
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

        return getActiveDriver();
    }

    /**
     * Allows wrapping the the base driver supplied by
     * {@link #getWrappedDriver()} in an additional layer - such as that used by
     * Applitools-Eyes for visual regression checking.
     *
     * @param driver New driver
     */
    public void setDriver(WebDriver driver) {
    	if (WebDriverConfig.getInstance().isEventLoggingEnabled()) {
    		this.eventFiringDriver.unregister(this.eventListener);
	    	this.eventFiringDriver = new EventFiringWebDriver(driver);
	        this.eventFiringDriver.register(this.eventListener);
	    } else {
	    	// TODO Once we re-implement AppliToolsEyes support we need to test that this is the correct approach as we've now lost the original driver if we need to unwrap it 
	    	this.wrappedDriver = driver;
	    }
    }


    /**
     * Provides an HtmlElementsLoader that provides findElement(s) methods for HtmlElement based classes.
     *
     * @param pageObject PageObject sitting on
     * @return HtmlElementsLoader
     */
    public PageObjectAwareHtmlElementsLoader getHtmlElementsLoader(BasePageObject<?> pageObject) {
        return new PageObjectAwareHtmlElementsLoader(getActiveDriver(), pageObject);
    }

    /**
     * Opens a browser obtaining browser settings from configuration file.
     *
     * @return WebDriver
     */
    public WebDriver open() {
        if (this.browserProvider == null) {
            this.browserProvider = Browser.getConfiguredBrowserProvider();
        }

        return open(this.browserProvider);
    }

    private WebDriver getActiveDriver() {
        return this.eventFiringDriver == null ? this.wrappedDriver : this.eventFiringDriver;
    }
    
    /**
     * Opens a browser using supplied configuration.
     *
     * @param config Browser definition
     * @return WebDriver
     */
    public WebDriver open(BrowserProvider config) {
        if (getActiveDriver() != null ) {
            throw new RuntimeException("Browser is already open");
        }

        LOGGER.debug("Starting browser");

        this.browserProvider = config;
        this.wrappedDriver = config.createDriver();

        if (WebDriverConfig.getInstance().isEventLoggingEnabled()) {
    	    this.eventFiringDriver = new EventFiringWebDriver(this.wrappedDriver);        
            this.eventListener = new SeleniumEventLogger();
	        this.eventFiringDriver.register(this.eventListener);
        }
        
        if (isRemoteDriver()) {
            this.sessionId = ((RemoteWebDriver) getWrappedDriver()).getSessionId();
        } else {
            this.sessionId = null;
        }

        return getActiveDriver();
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
        	if (this.eventListener != null) {
        		this.eventFiringDriver.unregister(this.eventListener);
        	}

            getActiveDriver().quit();
        } catch (Exception ex) {
            LOGGER.warn("Exception attempting to quit the browser: " + ex.getMessage());
        }

        this.eventFiringDriver = null;
        this.wrappedDriver = null;
    }

    /**
     * @return The current browser provider
     */
    public BrowserProvider getBrowserProvider() {
        return this.browserProvider;
    }

    /**
     * Provide the configuration for the browser as selected in the
     * configuration file.
     *
     * @return Browser configuration
     */
    public static BrowserProvider getConfiguredBrowserProvider() {
        try {
            return (BrowserProvider) Class.forName(WebDriverConfig.getInstance().getBrowserProvider()).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Unable to create class " + WebDriverConfig.getInstance().getBrowserProvider(), e);
        }
    }

    /**
     * Register the Screenshot Taker.
     */
    public void registerScreenshotTaker() {
        if (!ReportLoggerFactory.hasScreenshotTaker()) {
            ScreenshotTaker screenshotTaker;

            try {
                screenshotTaker = getScreenshotTakerClass().getDeclaredConstructor(WebDriver.class).newInstance(wrappedDriver);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                if (e.getMessage() == null && e.getCause() != null) {
                    throw new RuntimeException(e.getCause());
                } else {
                    throw new RuntimeException(e);
                }
            }

            ReportLoggerFactory.setScreenshotTaker(screenshotTaker);
        }
    }

    /**
     * Remove the Screenshot Taker.
     */
    public void removeScreenshotTaker() {
        ReportLoggerFactory.removeScreenshotTaker();
    }

    public static void setScreenshotTakerClass(Class<? extends ScreenshotTaker> screenshotTakerClass) {
        screenshotTaker = screenshotTakerClass;
    }
    
    public Class<? extends ScreenshotTaker> getScreenshotTakerClass() {
        return screenshotTaker;
    }
}
