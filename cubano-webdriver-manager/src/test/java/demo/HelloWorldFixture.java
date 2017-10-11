package demo;

import java.util.List;

import org.concordion.api.AfterSuite;
import org.concordion.api.extension.Extension;
import org.concordion.cubano.driver.web.Browser;
import org.concordion.ext.LoggingFormatterExtension;
import org.concordion.ext.StoryboardExtension;
import org.concordion.ext.StoryboardLogListener;
import org.concordion.integration.junit4.ConcordionRunner;
import org.concordion.slf4j.ext.ReportLogger;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@RunWith(ConcordionRunner.class)
public class HelloWorldFixture {

    @Extension
    StoryboardExtension storyboard = new StoryboardExtension();

    @Extension
    LoggingFormatterExtension logging = new LoggingFormatterExtension().registerListener(new StoryboardLogListener(storyboard));

    static Browser browser = new Browser();

    ReportLogger logger = ReportLoggerFactory.getReportLogger(HelloWorldFixture.class);

    public String getGreetingFailure() throws InterruptedException {
        int attempts = 0;

        for (int i = 0; i < 1; i++) {
            attempts++;

            browser.getDriver().navigate().to("http://google.co.nz");

            browser.getDriver().findElement(By.cssSelector("input[name=q]")).sendKeys("concordion");

            browser.getDriver().findElement(By.cssSelector("input[name=btnK]")).click();

            WebDriverWait wait = new WebDriverWait(browser.getDriver(), 3);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("foot")));

            boolean exception = false;

            do {
                try {
                    List<WebElement> results = browser.getDriver().findElements(By.cssSelector("h3[class=r] > a"));

                    for (WebElement result : results) {
                        if (result.getAttribute("href").equals("http://concordion.org/")) {
                            result.click();
                            // Thread.sleep(1000);
                        }
                    }

                    exception = false;

                } catch (StaleElementReferenceException e) {
                    exception = true;
                }
            } while (exception);
        }

        // logger.with()
        // .message("Hello World!")
        // .attachment("This is some data", "data.txt", MediaType.PLAIN_TEXT)
        // .marker(new StoryboardMarker("Hello", "Data", StockCardImage.TEXT, CardResult.SUCCESS))
        // .debug();

        // return "Failed " + attempts;
        return "Hello World!";
    }

    @AfterSuite
    public void afterSuite() {
        browser.close();
    }
}
