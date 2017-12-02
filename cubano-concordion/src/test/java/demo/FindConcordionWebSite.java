package demo;

import java.util.List;

import org.concordion.cubano.driver.web.Browser;
import org.concordion.cubano.driver.web.SeleniumScreenshotTaker;
import org.concordion.cubano.framework.ConcordionBase;
import org.concordion.ext.StoryboardMarkerFactory;
import org.concordion.slf4j.ext.ReportLogger;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FindConcordionWebSite extends ConcordionBase {

    ReportLogger logger = ReportLoggerFactory.getReportLogger(FindConcordionWebSite.class);

    public String google(String term, String link) {
        Browser browser = getBrowser();

        WebDriverWait wait = new WebDriverWait(browser.getDriver(), 5);

        browser.getDriver().navigate().to("http://google.co.nz");

        logger.with()
                .message("Open")
                .screenshot(new SeleniumScreenshotTaker(browser.getDriver(), null))
                .marker(StoryboardMarkerFactory.addCard("Google"))
                .debug();

        logger.info("GOTO");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name=q]")));


        browser.getDriver().findElement(By.cssSelector("input[name=q]")).sendKeys(term);
        logger.info("TYPE");

        logger.with()
                .message("Search")
                .screenshot(new SeleniumScreenshotTaker(browser.getDriver(), browser.getDriver().findElement(By.cssSelector("input[name=q]"))))
                .marker(StoryboardMarkerFactory.addCard("Google"))
                .debug();

        browser.getDriver().findElement(By.cssSelector("input[name=q]")).sendKeys(Keys.ENTER);


        // browser.getDriver().findElement(By.cssSelector("input[name=btnK]")).click();
        logger.info("CLICK");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("foot")));
        logger.info("WAIT");

        boolean exception = false;

        do {
            try {
                List<WebElement> results = browser.getDriver().findElements(By.cssSelector("h3[class=r] > a"));

                for (WebElement result : results) {
                    if (result.getAttribute("href").equals(link)) {

                        logger.with()
                                .message("Click")
                                .screenshot(new SeleniumScreenshotTaker(browser.getDriver(), result))
                                .marker(StoryboardMarkerFactory.addCard("Google"))
                                .debug();

                        result.click();
                        logger.info("NAVIGATE");

                        break;
                        // Thread.sleep(1000);
                    }
                }

                exception = false;

            } catch (StaleElementReferenceException e) {
                exception = true;
            }
        } while (exception);

        wait.until(ExpectedConditions.urlContains(link));

        return browser.getDriver().getCurrentUrl();
    }
}
