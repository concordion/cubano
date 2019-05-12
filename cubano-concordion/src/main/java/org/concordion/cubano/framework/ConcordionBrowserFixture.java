package org.concordion.cubano.framework;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeExample;
import org.concordion.cubano.driver.BrowserBasedTest;
import org.concordion.cubano.driver.web.Browser;
import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.concordion.cubano.driver.web.provider.BrowserProvider;
import org.concordion.cubano.framework.resource.ResourceScope;
import org.concordion.ext.ScreenshotTaker;

/**
 * Concordion fixture for inheritance by any test classes that require a browser.
 * Includes and configures the Storyboard and Logging extensions.
 **/
public abstract class ConcordionBrowserFixture extends ConcordionFixture implements BrowserBasedTest {
    private static ThreadLocal<Map<String, Browser>> threadBrowsers = ThreadLocal.withInitial(HashMap::new);
    private static ThreadLocal<String> threadBrowserId = ThreadLocal.withInitial(() -> Browser.DEFAULT);
    private static ThreadLocal<Integer> browserTestCount = ThreadLocal.withInitial(() -> 0);
    private static ThreadLocal<Boolean> browserTestRunCounted = ThreadLocal.withInitial(() -> false);

    private static int browserCloseAfterXTests = WebDriverConfig.getInstance().getRestartBrowserAfterXTests();

    @BeforeExample
    private final void beforeExample() {
        // Done here rather than afterExample so that extension have a chance to to final screenshot
        Map<String, Browser> browsers = threadBrowsers.get();

        for (Iterator<String> iterator = browsers.keySet().iterator(); iterator.hasNext(); ) {
            Browser browser = browsers.get(iterator.next());

            browser.removeScreenshotTaker();
        }

        browserTestRunCounted.set(false);
    }

    public static <T extends ScreenshotTaker> void setDefaultScreenshotTakerClass(Class<? extends ScreenshotTaker> screenshotTaker) {
        Browser.setDefaultScreenshotTakerClass(screenshotTaker);
    }

    @Override
    public Browser getBrowser() {
        return getBrowser(threadBrowserId.get());
    }

    /**
     * Starts browser using the default browser provider (from config.properties), or if already open returns reference to it.
     * All subsequent requests for a browser handle will return this browser unless focus is switched back using {@link #getBrowser()} or {@link #switchBrowser(String)}.
     *
     * @param key Id of the browser to open / switch control to
     * @return Browser browser based on key.
     */
    public Browser getBrowser(String key) {
        return getBrowser(key, Browser.getConfiguredBrowserProvider());
    }

    /**
     * Starts browser using the supplied browser provider, or if already open returns reference to it.
     * All subsequent requests for a browser handle will return this browser unless switched.
     *
     * @param key             Id of the browser to open / switch control to
     * @param browserProvider BrowserProvider to use if not already exist, otherwise ignored
     * @return Browser based on key
     */
    public Browser getBrowser(String key, BrowserProvider browserProvider) {
        incrementBrowserTestCount();

        Map<String, Browser> browsers = threadBrowsers.get();

        if (browsers.get(key) == null) {
            Browser newBrowser = new Browser(browserProvider);

            browsers.put(key, newBrowser);
            if(!isRegistered(browserProvider, ResourceScope.SUITE)) {
                registerCloseableResource(browserProvider, ResourceScope.SUITE);
            }
            registerCloseableResource(newBrowser, ResourceScope.SUITE);
        }

        if (threadBrowserId.get() != key) {
            threadBrowserId.set(key);
        }

        return browsers.get(key);
    }

    /**
     * Switches control to specified browser. Works much the same as {@link #getBrowser(String)} except that it will not start browser if not already open.
     * <p>
     * e.g. {@code switchBrowser(Browser.DEFAULT);}
     * </p>
     *
     * @param key Id of the browser to open / switch control to
     */
    public void switchBrowser(String key) {
        Map<String, Browser> browsers = threadBrowsers.get();
        Browser browser = browsers.get(key);

        if (browser == null) {
            throw new IllegalStateException("No browser exists for key " + key);
        }

        threadBrowserId.set(key);

        // Attempt to set focus to the newly selected browser
        try {
            // https://github.com/SeleniumHQ/selenium/issues/3560
            // JavascriptExecutor executor = (JavascriptExecutor) browser.getDriver();
            // executor.executeScript("alert(\"Focus window\")");
            //
            // PageHelper.waitUntil(browser.getDriver(), ExpectedConditions.alertIsPresent(), 1);
            //
            // Alert alert = browser.getDriver().switchTo().alert();
            //
            // alert.accept();

            // Assuming we are switching to a single window in the browser
            for (String winHandle : browser.getDriver().getWindowHandles()) {
                browser.getDriver().switchTo().window(winHandle);
            }

        } catch (Exception e) {
            logger.warn("Unable to set focus to the newly selected browser");
        }
    }

    public void switchBrowser(String key, BrowserProvider browserProvider) {
        getBrowser(key, browserProvider);
        switchBrowser(key);
    }

    private void incrementBrowserTestCount() {
        if (browserCloseAfterXTests <= 0 || browserTestRunCounted.get()) {
            return;
        }

        browserTestRunCounted.set(true);
        Integer count = browserTestCount.get();

        if (count >= browserCloseAfterXTests) {
            Map<String, Browser> browsers = threadBrowsers.get();

            for (Iterator<String> iterator = browsers.keySet().iterator(); iterator.hasNext(); ) {
                Browser browser = browsers.get(iterator.next());

                browser.close();
            }

            browserTestCount.set(0);
        } else {
            browserTestCount.set(count + 1);
        }
    }

    @AfterSuite
    public void resetThreadBrowsers() {
        threadBrowsers.remove();
        threadBrowserId.remove();
        browserTestCount.remove();
        browserTestRunCounted.remove();
    }
}