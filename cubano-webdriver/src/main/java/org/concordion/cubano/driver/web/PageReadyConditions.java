package org.concordion.cubano.driver.web;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Provides expected conditions to check that a page has completed any ajax requests.
 *
 * @author Andrew Sumner
 */
public class PageReadyConditions {
    private PageReadyConditions() {

    }

    /**
     * Returns true if there are no active Ajax requests.
     *
     * @return True if not using jQuery or  no active Ajax requests.
     */
    public static final ExpectedCondition<Boolean> noActiveAjaxRequest() {
        return new ExpectedCondition<Boolean>() {
            private static final String SCRIPT = "if (typeof jQuery === 'undefined') return true;\n" +
                    "if (jQuery.active != 0) return false;\n" +
                    "return true;";

            @Override
            public Boolean apply(WebDriver driver) {
                Boolean result;
                try {
                    result = (Boolean) ((JavascriptExecutor) driver).executeScript(SCRIPT);
                } catch (Exception ex) {
                    result = Boolean.FALSE;
                }
                return result;
            }

            @Override
            public String toString() {
                return "no active Ajax requests";
            }
        };
    }

    /**
     * @return True if there are no visible elements with the 'spinner' class name.
     */
    public static final ExpectedCondition<Boolean> noVisibleSpinners() {
//      private static final String SCRIPT =
//                "$('.Spinner').each(function() {\n" +
//                        "     if ($(this).css('display') != 'none') {\n" +
//                        "         return false;\n" +
//                        "     }\n" +
//                        "});\n" +
//                        "return true;";

        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                Boolean loaded = true;

                try {
                    List<WebElement> spinners = driver.findElements(By.className("Spinner"));
                    for (WebElement spinner : spinners) {
                        if (spinner.isDisplayed()) {
                            loaded = false;
                            break;
                        }
                    }
                } catch (Exception ex) {
                    loaded = false;
                }

                return loaded;
            }

            @Override
            public String toString() {
                return "no spinner elements to be visible";
            }
        };
    }

    /**
     * @return True if have switched to main document
     */
    public static final ExpectedCondition<Boolean> switchToMainDocument() {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                if (driver == null) {
                    return false;
                }

                PageHelper.switchToMainDocument(driver);
                return true;
            }

            @Override
            public String toString() {
                return "unable to switch to main document";
            }
        };
    }
}
