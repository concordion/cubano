package demo;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestBug {
    @Test
    public void run() {
        FirefoxOptions options = new FirefoxOptions();

        System.setProperty("webdriver.gecko.driver", "C:/Users/andre/.m2/repository/webdriver/geckodriver/win64/0.19.0/geckodriver.exe");
        options.setLegacy(false);

        FirefoxProfile profile = new FirefoxProfile();

        options.setProfile(profile);

        WebDriver driver = new FirefoxDriver(options);

        getBrowserToDoStuffTillItRunsOutOfMemory(driver);

        driver.quit();
    }

    private void getBrowserToDoStuffTillItRunsOutOfMemory(WebDriver driver) {
        for (int i = 0; i < 200; i++) {
            driver.navigate().to("http://google.co.nz");
            driver.findElement(By.cssSelector("input[name=q]")).sendKeys("concordion");
            driver.findElement(By.cssSelector("input[name=btnK]")).click();

            WebDriverWait wait = new WebDriverWait(driver, 3);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("foot")));

            boolean exception = false;

            do {
                try {
                    List<WebElement> results = driver.findElements(By.cssSelector("h3[class=r] > a"));

                    for (WebElement result : results) {
                        if (result.getAttribute("href").equals("https://concordion.org/")) {
                            result.click();
                        }
                    }

                    exception = false;

                } catch (StaleElementReferenceException e) {
                    exception = true;
                }
            } while (exception);
        }
    }
}
