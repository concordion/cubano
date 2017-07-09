package org.concordion.cubano.driver.web;

import org.concordion.cubano.driver.web.pagefactory.PageObjectAware;
import org.concordion.slf4j.ext.ReportLogger;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import ru.yandex.qatools.htmlelements.element.HtmlElement;

/**
 * Encapsulate a block of html elements on a page.
 * <p>
 * <p>
 * This object has automatic access to the page object containing this components as well as the Browser and PageHelper classes.
 * </p>
 *
 * @author Andrew Sumner
 */
public class PageComponent extends HtmlElement implements PageObjectAware {
    private static final ReportLogger LOGGER = ReportLoggerFactory.getReportLogger(PageComponent.class);

    private BasePageObject<?> containingPage;
    private PageHelper pageHelper;
    private Browser browser;

    @Override
    public void setPageObject(BasePageObject<?> pageObject) {
        this.containingPage = pageObject;
        this.pageHelper = new PageHelper(pageObject);
        this.browser = pageObject.getBrowser();
    }

    protected BasePageObject<?> getContainingPage() {
        return containingPage;
    }

    protected PageHelper getPageHelper() {
        return pageHelper;
    }

    protected Browser getBrowser() {
        return browser;
    }

    protected ReportLogger getLogger() {
        return LOGGER;
    }

    /**
     * A convenience method for executing ExpectedConditions.
     *
     * @param condition        The expected condition
     * @param timeOutInSeconds how long to wait for the expected condition to evaluate to true
     * @throws org.openqa.selenium.TimeoutException If the timeout expires
     */
    protected void waitUntil(ExpectedCondition<?> condition, int timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(browser.getDriver(), timeOutInSeconds);
        wait.until(condition);
    }
}
