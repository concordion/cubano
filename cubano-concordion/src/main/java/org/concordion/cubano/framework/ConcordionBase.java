package org.concordion.cubano.framework;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeExample;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.api.option.MarkdownExtensions;
import org.concordion.cubano.driver.BrowserBasedTest;
import org.concordion.cubano.driver.web.Browser;
import org.concordion.cubano.driver.web.PageHelper;
import org.concordion.cubano.driver.web.config.WebDriverConfig;
import org.concordion.cubano.driver.web.provider.BrowserProvider;
import org.concordion.integration.junit4.ConcordionRunner;
import org.concordion.logback.LogbackAdaptor;
import org.junit.runner.RunWith;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up any Concordion extensions or other items that must be shared between index and test fixtures.
 * 
 * NOTE: Test can be run from a Fixture or an Index, any global (@...Suite) methods must be in this class
 * to ensure the are executed from whichever class initiates the test run.
 */
@RunWith(ConcordionRunner.class)
@ConcordionOptions(markdownExtensions = { MarkdownExtensions.HARDWRAPS, MarkdownExtensions.AUTOLINKS })
public abstract class ConcordionBase implements BrowserBasedTest {
    private static List<Browser> allBrowsers = new ArrayList<Browser>();
    private static ThreadLocal<Map<String, Browser>> threadBrowsers = ThreadLocal.withInitial(HashMap::new);
    private static ThreadLocal<String> threadBrowserId = ThreadLocal.withInitial(() -> Browser.DEFAULT);
    private static ThreadLocal<Integer> testCount = ThreadLocal.withInitial(() -> 0);
    private static ThreadLocal<Boolean> browserTestRunCounted = ThreadLocal.withInitial(() -> false);

    private static int browserCloseAfterXTests = WebDriverConfig.getInstance().getRestartBrowserAfterXTests();

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcordionBase.class);

    // TODO Want this in here but 'bug' in concordion and/or extension where storyboard ends up on all pages, including indexes using cards from linked tests
    // @Extension
    // private final StoryboardExtension storyboard = new StoryboardExtension();
    //
    // @Extension
    // private final LoggingFormatterExtension loggerExtension = new LoggingFormatterExtension()
    // .registerListener(new StoryboardLogListener(getStoryboard()));

    static {
        LogbackAdaptor.logInternalStatus();
    }

    @BeforeExample
    private final void beforeExample() {
        // Done here rather than afterExample so that extension have a chance to to final screenshot
        Map<String, Browser> browsers = threadBrowsers.get();

        for (Iterator<String> iterator = browsers.keySet().iterator(); iterator.hasNext();) {
            Browser browser = browsers.get(iterator.next());

            browser.removeScreenshotTaker();
        }

        browserTestRunCounted.set(false);
    }

    @AfterSuite
    private final void afterSuite() {
        for (Browser openbrowser : allBrowsers) {
            if (openbrowser != null) {
                openbrowser.close();

                if (isLastOfType(openbrowser)) {
                    openbrowser.getBrowserProvider().cleanup();
                }
            }
        }
    }

    private boolean isLastOfType(Browser browser) {
        if (browser.getBrowserProvider() == null)
            return false;

        if (allBrowsers.indexOf(browser) == allBrowsers.size() - 1)
            return true;

        return !allBrowsers.subList(allBrowsers.indexOf(browser) + 1, allBrowsers.size()).stream()
                .filter(e -> e.isOpen() && e.getBrowserProvider() != null && e.getBrowserProvider().getClass() == browser.getBrowserProvider().getClass()).findFirst().isPresent();
    }

    @Override
    public Browser getBrowser() {
        return getBrowser(threadBrowserId.get());
    }

    /**
     * Starts browser using the default browser provider (from config.properties), or if already open returns reference to it.
     * All subsequent requests for a browser handle will return this browser unless focus is switched back using {@link #getBrowser()} or {@link #switchBrowser()}.
     * 
     * @param key
     * @return
     */
    public Browser getBrowser(String key) {
        return getBrowser(key, null);
    }

    /**
     * Starts browser using the supplied browser provider, or if already open returns reference to it.
     * All subsequent requests for a browser handle will return this browser unless switched.
     * 
     * @param key Id of the browser to open / switch control to
     * @param browserProvider BrowserProvider to use if not already exist, otherwise ignored
     * @return
     */
    public Browser getBrowser(String key, BrowserProvider browserProvider) {
        incrementBrowserTestCount();

        Map<String, Browser> browsers = threadBrowsers.get();

        if (browsers.get(key) == null) {
            Browser newBrowser = new Browser(browserProvider);

            browsers.put(key, newBrowser);
            allBrowsers.add(newBrowser);
        }

        if (threadBrowserId.get() != key) {
            threadBrowserId.set(key);
        }

        return browsers.get(key);
    }

    /**
     * Switches control to specified browser. Works much the same as {@link #getBrowser(String)} except that it will not start browser if not already open.
     * <br/>
     * <br/>
     * e.g. {@code switchBrowser(Browser.DEFAULT);}
     * 
     * @param key
     * @return
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
            JavascriptExecutor executor = (JavascriptExecutor) browser.getDriver();
            executor.executeScript("alert(\"Focus window\")");
            
            PageHelper.waitUntil(browser.getDriver(), ExpectedConditions.alertIsPresent(), 1);

            Alert alert = browser.getDriver().switchTo().alert();
            
            alert.accept();
            
            
        } catch (Exception e) {
            LOGGER.warn("Unable to set focus to the newly selected browser");
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
        Integer count = testCount.get();

        if (count >= browserCloseAfterXTests) {
            Map<String, Browser> browsers = threadBrowsers.get();

            for (Iterator<String> iterator = browsers.keySet().iterator(); iterator.hasNext();) {
                Browser browser = browsers.get(iterator.next());

                browser.close();
            }

            testCount.set(0);
        } else {
            testCount.set(count + 1);
        }

    }

    // TODO See above todo
    // /**
    // * @return A reference to the Storyboard extension.
    // */
    // protected StoryboardExtension getStoryboard() {
    // return storyboard;
    // }
}
