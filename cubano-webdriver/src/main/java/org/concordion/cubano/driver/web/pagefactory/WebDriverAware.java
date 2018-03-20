package org.concordion.cubano.driver.web.pagefactory;

import org.openqa.selenium.WebDriver;

/**
 * Allows page components to perform navigation and other tasks and still use all the framework features.
 * 
 * <pre>
 * public class SomeComponent extends HtmlElement implements WebDriverAware {
 *     private WebDriver driver;
 *     private WebElement input;
 *
 *     {@literal @Override}
 *     public void setWebDriver(WebDriver driver) {
 *         this.driver = driver;
 *     }
 *
 *     public void enterText(String text) {
 *         WebDriverWait wait = new WebDriverWait(driver, 1);
 *         wait.until(ExpectedConditions.elementToBeClickable(input));
 *         input.setText(text);
 *     }
 * }
 * </pre>
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
