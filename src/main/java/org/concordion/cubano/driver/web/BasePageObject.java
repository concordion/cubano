package org.concordion.cubano.driver.web;

import java.util.List;

import org.concordion.cubano.driver.BrowserBasedTest;
import org.concordion.cubano.driver.web.pagefactory.PageObjectAwareHtmlElementDecorator;
import org.concordion.ext.ScreenshotTaker;
import org.concordion.ext.storyboard.CardResult;
import org.concordion.slf4j.ext.ReportLogger;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementLocatorFactory;

/**
 * Provides core functionality for pages objects using the page object factory pattern.
 *
 * @param <T> Class of the PageObject extending this class, so that we can implement fluent style methods
 * @author Andrew Sumner
 */
public abstract class BasePageObject<T extends BasePageObject<T>> {
    private final ReportLogger logger = ReportLoggerFactory.getReportLogger(this.getClass().getName());
    private BrowserBasedTest test;
    private PageHelper pageHelper;

    /**
     * Creates a new page object.
     *
     * @param test                 Calling class
     * @param timeoutWaitInSeconds Maximum time to wait for page to finish loading
     * @param params               An optional set of parameters to pass to the waitUntilPageIsLoaded method
     */
    protected BasePageObject(BrowserBasedTest test, int timeoutWaitInSeconds, Object... params) {
        this.test = test;
        this.pageHelper = new PageHelper(this, BasePageObject.class);

        getLogger().step("Page Object: {}", getSimpleName());

        refreshPageElements();
        waitUntilPageIsLoaded(timeoutWaitInSeconds, params);
    }

    /**
     * Updates all fields in this class (and descendant classes) with new pointers to the elements on the webpage we're pointing at.
     * <p>
     * This is useful in the event that an Ajax request has reloaded large portions of your page and to avoid StaleElementExcpetions it is easier just to
     * refresh all fields that work out which ones need updating.
     */
    public void refreshPageElements() {
        PageFactory.initElements(new PageObjectAwareHtmlElementDecorator(new HtmlElementLocatorFactory(getBrowser().getDriver()), this), this);
    }

    /**
     * Has two purposes:
     * <p>
     * <ul>
     * <li>Check that an element on the page that uniquely identifies this page is present</li>
     * <li>Ensure that the page is fully loaded (eg Ajax requests have completed) so don't have to implement waits when accessing elements on the page</li>
     * </ul>
     * <p>
     * All PageObjects must implement this method.
     *
     * @param timeoutWaitInSeconds Number of seconds to wait for the page to load before timing out
     * @param params               List of parameters passed to the constructor, if any
     */
    protected abstract void waitUntilPageIsLoaded(int timeoutWaitInSeconds, Object... params);

    /**
     * @return The name of the current page object.
     */
    public String getSimpleName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Provide a reference to the logger implemented for the current page object.
     *
     * @return Logger
     */
    public ReportLogger getLogger() {
        return logger;
    }

    /**
     * Provide a reference to the calling class that implements the TestDriveable interface.
     *
     * @return Reference to class implementing the TestDriveable interface
     */
    public BrowserBasedTest getTest() {
        return test;
    }

    /**
     * Opens a browser if not already open and return a reference to it.
     *
     * @return A reference to the current browser
     */
    public Browser getBrowser() {
        return test.getBrowser();
    }

    /**
     * Perform a visual comparison of the window with a baseline image.
     *
     * @return A self reference
     */
    @SuppressWarnings("unchecked")
    public T checkWindow() {
        pageHelper.checkWindow(getSimpleName());
        return (T) this;
    }

    /**
     * Perform a visual comparison of the window with a baseline image.
     *
     * @param region Element on page to compare
     * @return A self reference
     */
    @SuppressWarnings("unchecked")
    public T checkRegion(WebElement region) {
        pageHelper.checkRegion(region, getSimpleName());
        return (T) this;
    }

    /**
     * IE will often show a security certificate warning page when accessing a website, if present select continue to website.
     *
     * @param browser Browser
     */
    protected static void ignoreSecurityCertificateWarning(Browser browser) {
        // For some reason unable to access elements on this page, javascript workaround is required
        if (browser.getDriver().getTitle().equals("Certificate Error: Navigation Blocked")) {
            browser.getDriver().navigate().to("javascript:document.getElementById('overridelink').click()");
        }
    }

    /**
     * Check to see if the element is on the page or not.
     *
     * @param element WebElement to search for
     * @return found or not
     */
    protected boolean isElementPresent(WebElement element) {
        return pageHelper.isElementPresent(element);
    }

    /**
     * Check to see if the element is on the page or not.
     *
     * @param driver Reference to WebDriver
     * @param by     How to find the element
     * @return Is the element on the page
     */
    protected static boolean isElementPresent(WebDriver driver, By by) {
        return PageHelper.isElementPresent(driver, by);
    }

    /**
     * Notify listener that it should take a screenshot of the current page.
     * <p>
     * <p>A default description is constructed assuming that a click is about to be performed on an element
     * in the format: {@literal Clicking '<element text>'}</p>
     *
     * @param element Element to highlight, or null if not applicable
     * @return A self reference
     */
    @SuppressWarnings("unchecked")
    protected T capturePage(WebElement element) {
        pageHelper.capturePage(element);
        return (T) this;
    }

    /**
     * Notify listener that it should take a screenshot of the current page.
     *
     * @param description Description to include with screenshot
     * @return A self reference
     */
    @SuppressWarnings("unchecked")
    public T capturePage(String description) {
        pageHelper.capturePage((WebElement) null, description);
        return (T) this;
    }

    /**
     * Notify listener that it should take a screenshot of the current page.
     *
     * @param element     Element to highlight, or null if not applicable
     * @param description Description to include with screenshot
     * @return A self reference
     */
    @SuppressWarnings("unchecked")
    public T capturePage(WebElement element, String description) {
        pageHelper.capturePage(element, description);
        return (T) this;
    }

    /**
     * Notify listener that it should take a screenshot of the current page.
     *
     * @param screenshotTaker Custom screenshot taker
     * @param description     Description to include with screenshot
     * @return A self reference
     */
    @SuppressWarnings("unchecked")
    public T capturePage(ScreenshotTaker screenshotTaker, String description) {
        pageHelper.capturePage(screenshotTaker, description);
        return (T) this;
    }

    /**
     * Notify listener that it should take a screenshot of the current page.
     *
     * @param element     Element to highlight, or null if not applicable
     * @param description Description to include with screenshot
     * @param result      Status
     * @return A self reference
     */
    @SuppressWarnings("unchecked")
    public T capturePage(WebElement element, String description, CardResult result) {
        pageHelper.capturePage(element, description, result);
        return (T) this;
    }

    /**
     * Notify listener that it should take a screenshot of the current page and then click the supplied element
     * and return a new instance of the expected page.
     * <p>
     * <p>A default description is constructed in the format: {@literal Clicking '<element text>'}</p>
     *
     * @param <P>          The type of the desired page object
     * @param element      Element to click
     * @param expectedPage Class of page that should be returned
     * @return new PageObject of type expectedPage
     */
    public <P extends BasePageObject<P>> P capturePageAndClick(WebElement element, Class<P> expectedPage) {
        return pageHelper.capturePageAndClick(element, expectedPage);
    }

    /**
     * Notify listener that it should take a screenshot of the current page and then click the supplied element
     * and return a new instance of the expected page.
     *
     * @param <P>          The type of the desired page object
     * @param element      Element to click
     * @param description  Description of the action being taken
     * @param expectedPage Class of page that should be returned
     * @return new PageObject of type expectedPage
     */
    public <P extends BasePageObject<P>> P capturePageAndClick(WebElement element, String description, Class<P> expectedPage) {
        return pageHelper.capturePageAndClick(element, description, expectedPage);
    }

    /**
     * Helper method for creating new page objects from an expected class
     * <p>
     * Requires that the expectedPage extends from PageObject and has a public constructor with a single
     * parameter of TestDriveable.
     *
     * @param <P>          The type of the desired page object
     * @param expectedPage Page that should be returned
     * @return New instance of supplied page object
     */
    public <P extends BasePageObject<P>> P newInstance(Class<P> expectedPage) {
        return pageHelper.newInstance(expectedPage);
    }

    /**
     * Find the first visible element in a list.
     *
     * @param webElements list of elements to search through
     * @return A visible WebElement or null if no visible elements in list
     */
    protected WebElement getFirstVisibleElement(List<WebElement> webElements) {
        return pageHelper.getFirstVisibleElement(webElements);
    }

    /**
     * Override this method to implement any code that needs to be run prior to taking a screen shot,
     * eg fixing floating headers.
     */
    public void prepareForScreenshot() {

    }

    //CHECKSTYLE:OFF - Not handling TimeoutException

    /**
     * A convenience method for executing ExpectedConditions.
     *
     * @param condition        The expected condition
     * @param timeOutInSeconds how long to wait for the expected condition to evaluate to true
     * @throws org.openqa.selenium.TimeoutException If the timeout expires
     */
    //CHECKSTYLE:ON
    protected void waitUntil(ExpectedCondition<?> condition, int timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(getBrowser().getDriver(), timeOutInSeconds);
        wait.until(condition);
    }

    /**
     * Similar to WebDriver's switchTo().{@link TargetLocator#defaultContent()} but will always select the main document when a page contains iframes.
     */
    public void switchToMainDocument() {
        pageHelper.switchToMainDocument();
    }

    /**
     * Get the current frame's name or id property.
     *
     * @return Empty string if main document selected otherwise will return name property if set, id property if set, else 'UNKNOWN FRAME'
     * for selected iframe.
     */
    public String getCurrentFrameNameOrId() {
        return pageHelper.getCurrentFrameNameOrId();
    }
}
