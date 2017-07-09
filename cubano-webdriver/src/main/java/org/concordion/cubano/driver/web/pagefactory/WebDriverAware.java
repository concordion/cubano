package org.concordion.cubano.driver.web.pagefactory;

import org.openqa.selenium.WebDriver;

/**
 * Allows page components to perform navigation and other tasks and still use all the framework features.
 * <p>
 * <p><pre>
 * public class SomeComponent extends HtmlElement implements WebDriverAware {
 *     private WebDriver driver;
 *     private WebElement input;
 * <p>
 *     {@literal @Override}
 *     public void setWebDriver(WebDriver driver) {
 *         this.driver = driver;
 *     }
 * <p>
 *     public void enterText(String text) {
 *         WebDriverWait wait = new WebDriverWait(driver, 1);
 *         wait.until(ExpectedConditions.elementToBeClickable(input));
 * <p>
 *         input.setText(text);
 *     }
 * }
 * </pre></p>
 *
 * @author Andrew Sumner
 */
public interface WebDriverAware {
    /**
     * Sets the web driver for the implementing class.
     *
     * @param driver WebDriver
     */
    public void setWebDriver(WebDriver driver);
}
