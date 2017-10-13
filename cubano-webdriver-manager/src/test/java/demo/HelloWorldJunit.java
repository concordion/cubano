package demo;

import java.util.List;

import org.concordion.cubano.driver.web.Browser;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldJunit {

    Logger logger = LoggerFactory.getLogger(HelloWorldJunit.class);

    @Test
    public void test() throws InterruptedException {

        Browser browser = new Browser();
        WebDriverWait wait = new WebDriverWait(browser.getDriver(), 5);

        try {
            for (int i = 0; i < 1; i++) {

                browser.getDriver().navigate().to("http://google.co.nz");
                logger.info("GOTO");

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name=q]")));

                browser.getDriver().findElement(By.cssSelector("input[name=q]")).sendKeys("concordion");
                logger.info("TYPE");

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
                            if (result.getAttribute("href").equals("http://concordion.org/")) {
                                result.click();
                                logger.info("NAVIGATE");

                                break;
                            }
                        }

                        exception = false;

                    } catch (StaleElementReferenceException e) {
                        exception = true;
                    }
                } while (exception);

                wait.until(ExpectedConditions.titleContains("Concordion"));
            }
        } finally {
            browser.close();
        }
    }
}
