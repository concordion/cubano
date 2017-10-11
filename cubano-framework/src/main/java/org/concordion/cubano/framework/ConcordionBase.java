package org.concordion.cubano.framework;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeExample;
import org.concordion.api.extension.Extension;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.api.option.MarkdownExtensions;
import org.concordion.cubano.driver.BrowserBasedTest;
import org.concordion.cubano.driver.web.Browser;
import org.concordion.cubano.driver.web.provider.BrowserProvider;
import org.concordion.ext.StoryboardExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.concordion.logback.LogbackAdaptor;
import org.junit.runner.RunWith;

/**
 * Sets up any Concordion extensions or other items that must be shared between index and test fixtures.
 * 
 * NOTE: Test can be run from a Fixture or an Index, any global (@...Suite) methods must be in this class
 * to ensure the are executed from whichever class initiates the test run.
 */
@RunWith(ConcordionRunner.class)
// @ConcordionResources("/customConcordion.css")
// @Extensions({ TimestampFormatterExtension.class, RunTotalsExtension.class, ExpectedToFailInfoExtension.class })
@ConcordionOptions(markdownExtensions = { MarkdownExtensions.HARDWRAPS, MarkdownExtensions.AUTOLINKS })
public abstract class ConcordionBase implements BrowserBasedTest {
    private static final String DEFAULT = "default";

    private static List<Browser> allBrowsers = new ArrayList<Browser>();
    private static ThreadLocal<Map<String, Browser>> threadBrowsers = ThreadLocal.withInitial(HashMap::new);

    @Extension
    private final StoryboardExtension storyboard = new StoryboardExtension();

    // @Extension
    // private final EnvironmentExtension footer = new EnvironmentExtension()
    // .withRerunTest("MyMSD-RunSelectedTest")
    // .withRerunParameter("token", "ALLOW")
    // .withRerunParameter("TEST_CLASSNAME", this.getClass().getName().replace(ConcordionBase.class.getPackage().getName() + ".", ""))
    // .withEnvironment(AppConfig.getEnvironment().toUpperCase())
    // .withURL(AppConfig.getUrl());

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
    }

    @AfterSuite
    private final void afterSuite() {
        for (Browser openbrowser : allBrowsers) {
            if (openbrowser != null) {
                openbrowser.close();
            }
        }
    }

    @Override
    public Browser getBrowser() {
        return getBrowser(DEFAULT);
    }

    public Browser getBrowser(String key) {
        Map<String, Browser> browsers = threadBrowsers.get();

        if (browsers.get(key) == null) {
            Browser newBrowser = new Browser();

            browsers.put(key, newBrowser);
            allBrowsers.add(newBrowser);
        }

        return browsers.get(key);
    }

    public Browser getBrowser(String key, BrowserProvider browserProvider) {
        Map<String, Browser> browsers = threadBrowsers.get();

        if (browsers.get(key) == null) {
            Browser newBrowser = new Browser(browserProvider);

            browsers.put(key, newBrowser);
            allBrowsers.add(newBrowser);
        }

        return browsers.get(key);
    }

    /**
     * @return A reference to the Storyboard extension.
     */
    protected StoryboardExtension getStoryboard() {
        return storyboard;
    }
}
