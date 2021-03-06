package org.concordion.cubano.driver.web;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.concordion.cubano.driver.BrowserBasedTest;
import org.concordion.ext.ScreenshotTaker;
import org.concordion.ext.StoryboardMarker;
import org.concordion.ext.StoryboardMarkerFactory;
import org.concordion.ext.storyboard.CardResult;
import org.concordion.slf4j.ext.FluentLogger;
import org.concordion.slf4j.ext.ReportLogger;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Helper class that allows PageObject and PageComponent to share functionality.
 *
 * @author Andrew Sumner
 */
public class PageHelper {
    private static final ReportLogger LOGGER = ReportLoggerFactory.getReportLogger(PageHelper.class);

    private final BasePageObject<?> pageObject;
    private final Class<?> logLocation;

    /**
     * Constructor.
     *
     * @param pageObject PageObject this class belongs to
     */
    public PageHelper(BasePageObject<?> pageObject) {
        this.pageObject = pageObject;
        this.logLocation = null;
    }

    /**
     * Constructor.
     *
     * @param pageObject PageObject this class belongs to
     * @param logLocation To help with location aware logging
     */
    public PageHelper(BasePageObject<?> pageObject, Class<?> logLocation) {
        this.pageObject = pageObject;
        this.logLocation = logLocation;
    }

    /**
     * Notify and attached listeners that the current page should perform a visual regression check.
     *
     * @param triggerElement Element to check, or null to check the entire window
     * @param tag An optional tag to be associated with the snapshot
     */
    public void triggerCheckPage(WebElement triggerElement, String tag) {
        throw new UnsupportedOperationException();

        // PageEvent event = new PageEvent(pageObject, triggerElement, tag);
        //
        // pageObject.getListener().checkWindow(event);
    }

    /**
     * Perform a visual comparison of the window with a baseline image.
     *
     * @param tag An optional tag to be associated with the snapshot
     */
    public void checkWindow(String tag) {
        triggerCheckPage(null, tag);
    }

    /**
     * Perform a visual comparison of the window with a baseline image.
     *
     * @param region Region of page to check
     * @param tag An optional tag to be associated with the snapshot
     */
    public void checkRegion(WebElement region, String tag) {
        triggerCheckPage(region, tag);
    }

    /**
     * Check to see if the element is present, but not necessarily visible.
     *
     * @param element WebElement to search for
     * @return found or not
     */
    public boolean isElementPresent(WebElement element) {
        try {
            element.isDisplayed();
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    /**
     * Check to see if the element is visible or not.
     *
     * @param element WebElement to search for
     * @return found or not
     */
    public boolean isElementVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    /**
     * Check to see if the element is present, but not necessarily visible.
     *
     * @param driver Reference to WebDriver
     * @param by How to find the element
     * @return Is the element on the page
     */
    public static boolean isElementPresent(WebDriver driver, By by) {
        try {
            driver.findElement(by).isDisplayed();
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    /**
     * Check to see if the element is visible or not.
     *
     * @param driver Reference to WebDriver
     * @param by How to find the element
     * @return Is the element on the page
     */
    public static boolean isElementVisible(WebDriver driver, By by) {
        try {
            return driver.findElement(by).isDisplayed();
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    /**
     * Capture a screenshot of the current page and add it to the log and story board.
     * 
     * <p>
     * A default description is constructed assuming that a click is about to be performed on an element
     * in the format: {@literal Clicking '<element text>'}
     * </p>
     *
     * @param element Element to highlight, or null if not applicable
     */
    public void capturePage(WebElement element) {
        capturePage(element, getClickMessage(element));
    }

    /**
     * Capture a screenshot of the current page and add it to the log and story board.
     *
     * @param element Element to highlight, or null if not applicable
     * @param description Description to include with screenshot
     */
    public void capturePage(WebElement element, String description) {
        capturePage(element, description, CardResult.SUCCESS);
    }

    /**
     * Capture a screenshot of the current page and add it to the log and story board.
     *
     * @param screenshotTaker A custom screenshot taker
     * @param description Description to include with screenshot
     */
    public void capturePage(ScreenshotTaker screenshotTaker, String description) {

        capture(screenshotTaker, description, StoryboardMarkerFactory.addCard(pageObject.getSimpleName()));
    }

    /**
     * Capture a screenshot of the current page and add it to the log and story board.
     *
     * @param element Element to highlight, or null if not applicable
     * @param description Description to include with screenshot
     * @param result Status
     */
    public void capturePage(WebElement element, String description, CardResult result) {

        ScreenshotTaker screenshotTaker;

        try {
            screenshotTaker = pageObject.getBrowser().getScreenshotTakerClass()
                    .getDeclaredConstructor(WebDriver.class, WebElement.class)
                    .newInstance(pageObject.getBrowser().getDriver(), element);

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            if (e.getMessage() == null && e.getCause() != null) {
                throw new RuntimeException(e.getCause());
            } else {
                throw new RuntimeException(e);
            }
        }
        
        capture(screenshotTaker,
                description,
                StoryboardMarkerFactory.addCard(pageObject.getSimpleName(), null, result));
    }

    private void capture(ScreenshotTaker screenshotTaker, String description, StoryboardMarker storyboardMarker) {

        FluentLogger flogger = pageObject.getLogger().with()
                .message(description)
                .screenshot(screenshotTaker)
                .marker(storyboardMarker);

        if (logLocation != null) {
            flogger.locationAwareParent(logLocation);
        }

        flogger.debug();
    }

    /**
     * Capture a screenshot of the current page and add it to the log and story board and then click the supplied element
     * and return a new instance of the expected page.
     * 
     * <p>
     * A default description is constructed in the format: {@literal Clicking '<element text>'}
     * </p>
     *
     * @param <P> The type of the desired page object
     * @param element Element to click
     * @param expectedPage Class of page that should be returned
     * @param params Optional list of parameters that can be passed in to the constructor
     * @return new PageObject of type expectedPage
     */
    public <P extends BasePageObject<P>> P capturePageAndClick(WebElement element, Class<P> expectedPage, Object... params) {
        return capturePageAndClick(element, getClickMessage(element), expectedPage, params);
    }

    /**
     * Capture a screenshot of the current page and add it to the log and story board and then click the supplied element
     * and return a new instance of the expected page.
     * 
     * @param <P> The type of the desired page object
     * @param element Element to click
     * @param description Description of the action being taken
     * @param expectedPage Class of page that should be returned
     * @param params Optional list of parameters that can be passed in to the constructor
     * @return new PageObject of type expectedPage
     */
    public <P extends BasePageObject<P>> P capturePageAndClick(WebElement element, String description, Class<P> expectedPage, Object... params) {
        return capturePageAndClick(element, 10, description, expectedPage, params);
    }

    /**
     * Capture a screenshot of the current page and add it to the log and story board and then click the supplied element
     * and return a new instance of the expected page.
     *
     * @param <P> The type of the desired page object
     * @param element Element to click
     * @param timeoutSeconds Timeout in Seconds
     * @param description Description of the action being taken
     * @param expectedPage Class of page that should be returned
     * @param params Optional list of parameters that can be passed in to the constructor
     * @return new PageObject of type expectedPage
     */
    public <P extends BasePageObject<P>> P capturePageAndClick(WebElement element, int timeoutSeconds, String description, Class<P> expectedPage, Object... params) {
        waitForElementToClickable(element, timeoutSeconds);

        capturePage(element, description);

        waitAndClickElement(element, timeoutSeconds);

        if (expectedPage == null) {
            return null;
        }

        return newInstance(expectedPage, params);
    }

    /**
     * Wait for the element to be clickable.
     * 
     * Ignore {@link WebDriverException}. When a DOM operation is happening on a page it may
     * temporarily cause the element to be inaccessible. Ignore these hierarchy of Exceptions.
     * 
     * @param webElement The element to check is clickable.
     * @param timeOutInSeconds Timeout in Seconds.
     * 
     */
    public void waitForElementToClickable(WebElement webElement, int timeOutInSeconds) {

        new WebDriverWait(this.pageObject.getBrowser().getDriver(), timeOutInSeconds).ignoring(WebDriverException.class)
                .until((WebDriver d) -> {

                    ExpectedConditions.elementToBeClickable(webElement);
                    return true;

                });
    }

    /**
     * Wait and click the element.
     * 
     * Ignore {@link WebDriverException}. When a DOM operation is happening on a page it may
     * temporarily cause the element to be inaccessible. Ignore these hierarchy of Exceptions.
     * 
     * @param webElement Can click the element.
     * @param timeOutInSeconds Timeout in Seconds.
     * 
     */
    private void waitAndClickElement(WebElement element, int timeoutSeconds) {

        new WebDriverWait(this.pageObject.getBrowser().getDriver(), timeoutSeconds).ignoring(WebDriverException.class)
                .until((WebDriver d) -> {

                    element.click();
                    return true;

                });
    }

    /**
     * Helper method for creating new page objects from an expected class.
     * <p>
     * Requires that the expectedPage extends from PageObject and has a public constructor with a single
     * parameter of TestDriveable.
     *
     * @param <P> The type of the desired page object
     * @param expectedPage Page that should be returned
     * @param params Optional list of parameters that can be passed in to the constructor
     * @return New instance of supplied page object
     */
    public <P extends BasePageObject<P>> P newInstance(Class<P> expectedPage, Object... params) {
        try {

            // Account for PageObjects that only have a BrowserBasedTest constructor.
            if (params.length > 0) {
                Class<?>[] constructorArguments = new Class<?>[2];
                constructorArguments[0] = BrowserBasedTest.class;
                constructorArguments[1] = Object[].class;

                return expectedPage.getDeclaredConstructor(constructorArguments).newInstance(getTest(), params);

            } else {
                return expectedPage.getDeclaredConstructor(BrowserBasedTest.class).newInstance(getTest());
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            if (e.getMessage() == null && e.getCause() != null) {
                throw new RuntimeException(e.getCause());
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Find the first visible element in a list.
     *
     * @param webElements list of elements to search through
     * @return A visible WebElement or null if no visible elements in list
     */
    public WebElement getFirstVisibleElement(List<WebElement> webElements) {
        for (WebElement webElement : webElements) {
            if (webElement.isDisplayed()) {
                return webElement;
            }
        }

        return null;
    }

    /**
     * Get reference to TestDriveable interface for access to storyboard, etc.
     *
     * @return Reference to the test
     */
    private BrowserBasedTest getTest() {
        return pageObject.getTest();
    }

    /**
     * Get the current frame's name or id property.
     *
     * @return Empty string if main document selected otherwise will return name property if set, id property if set, else 'UNKNOWN FRAME'
     *         for selected iframe.
     */
    public String getCurrentFrameNameOrId() {
        return getCurrentFrameNameOrId(pageObject.getBrowser().getDriver());
    }

    /**
     * Get the current frame's name or id property.
     *
     * @param driver WebDriver
     * @return Empty string if main document selected otherwise will return name property if set, id property if set, else 'UNKNOWN FRAME'
     *         for selected iframe.
     */
    public static String getCurrentFrameNameOrId(WebDriver driver) {
        String script = "var frame = window.frameElement;" +
                "if (!frame) {" +
                "    return '';" +
                "}" +
                "if (frame.name) {" +
                "    return frame.name;" +
                "}" +
                "if (frame.id) {" +
                "    return frame.id;" +
                "}" +
                "return 'UNKNOWN FRAME';";

        return (String) ((JavascriptExecutor) driver).executeScript(script);
    }

    /**
     * Get the current frame.
     *
     * @param driver WebDriver
     * @return Null if main document selected otherwise frame WebElement.
     */
    public static WebElement getCurrentFrame(WebDriver driver) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript("return window.frameElement");
    }

    /**
     * Similar to WebDriver's switchTo().{@link TargetLocator#defaultContent()} but will always select the main document when a page contains iframes.
     */
    public void switchToMainDocument() {
        switchToMainDocument(pageObject.getBrowser().getDriver());
    }

    /**
     * Similar to WebDriver's switchTo().{@link TargetLocator#defaultContent()} but will always select the main document when a page contains iframes.
     *
     * @param driver WebDriver
     */
    public static void switchToMainDocument(WebDriver driver) {
        // Firefox gecko driver has a lot of issues with iframes, this method now has been updated to work around this
        // Hopefully can remove this line at some point
        // See: https://github.com/mozilla/geckodriver/issues/937
        driver.switchTo().defaultContent();

        driver.switchTo().window(driver.getWindowHandle());

        // As above - hopefully can remove this at some point
        for (int i = 0; i < 10; i++) {
            try {
                String currentFrame = getCurrentFrameNameOrId(driver);

                if (currentFrame.isEmpty()) {
                    break;
                }
            } catch (Throwable e) {
                // Do nothing
            }

            driver.switchTo().parentFrame();
        }
    }

    private String getClickMessage(WebElement element) {

        waitForElementToClickable(element, 10);

        String label = element.getText();

        if (label == null || label.isEmpty()) {
            label = element.getAttribute("value");
        }

        return "Clicking '" + label + "' ";
    }

    /**
     * Accept alert if one is found.
     */
    public void acceptAlertIfPresent() {
        acceptAlertIfPresent(pageObject.getBrowser().getDriver());
    }

    /**
     * Accept alert if one is found.
     *
     * @param driver WebDriver
     */
    public static void acceptAlertIfPresent(WebDriver driver) {
        try {
            waitUntil(driver, ExpectedConditions.alertIsPresent(), 1);
            Alert alert = driver.switchTo().alert();

            LOGGER.debug("Accepting alert \"{}\"", alert.getText());

            alert.accept();
        } catch (TimeoutException e) {
            // Do nothing
        }
    }

    /**
     * A helper method for using WebDriver explicit wait.
     *
     * @param driver WebDriver
     * @param condition Condition to check for
     * @param timeOutInSeconds Timeout value
     */
    public static void waitUntil(WebDriver driver, ExpectedCondition<?> condition, int timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.until(condition);
    }
}