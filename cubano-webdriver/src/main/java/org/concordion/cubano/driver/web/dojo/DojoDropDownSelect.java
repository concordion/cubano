package org.concordion.cubano.driver.web.dojo;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.concordion.cubano.driver.web.pagefactory.WebDriverAware;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import ru.yandex.qatools.htmlelements.element.TypifiedElement;

/**
 * A WebElement alternative for Dojo input controls.
 * 
 * <p>
 * The recommended location strategy for dojo elements is:
 * </p>
 * 
 * <pre>
 *     {@literal @FindBy}(css = "[id='uniqueid']")
 *     {@link DojoDropDownSelect} yourElementName;
 * </pre>
 * 
 * 
 * @author Andrew Sumner
 */
public class DojoDropDownSelect extends TypifiedElement implements WebDriverAware {
    private WebElement inputElement = null;
    private WebDriver driver;

    private Duration pollingEvery = Duration.of(500, ChronoUnit.MILLIS);
    private Duration timeout = Duration.of(10, ChronoUnit.SECONDS);

    @Override
    public void setWebDriver(WebDriver driver) {
        this.driver = driver;
    }

    public DojoDropDownSelect(WebElement wrappedElement) {
        super(wrappedElement);
    }

    public WebElement getInputElement() {
        if (inputElement == null) {
            WebElement element = super.getWrappedElement();

            if (element.getAttribute("class").contains("dijitInputInner")) {
                inputElement = element;
            } else {
                inputElement = element.findElement(By.xpath(".//input[contains(@class, 'dijitInputInner')]"));
            }
        }

        return inputElement;
    }

    public String getText() {
        return getInputElement().getAttribute("value");
    }

    /**
     * For a dijit select that performs a search and shows a list of available items.
     * 
     * @param charSequences Item to search for
     * 
     * @throws IllegalArgumentException if search text not found in dropdown list
     */
    public void select(CharSequence charSequence) {
        select(charSequence, true);
    }

    /**
     * For a dijit select that performs a search and shows a list of available items.
     * 
     * @param charSequences Item to search for
     * 
     * @return Returns false if search text not found in dropdown list
     */
    public boolean selectReturningResult(CharSequence charSequence) {
        return select(charSequence, false);
    }

    /**
     * 
     * @param timeout {@link Duration} to use for any timeout.
     * @return {@link DojoDropDownSelect}
     */
    public DojoDropDownSelect withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 
     * @param pollingEvery {@link Duration} to use for polling.
     * @return {@link DojoDropDownSelect}
     */
    public DojoDropDownSelect withPollingEvery(Duration pollingEvery) {
        this.pollingEvery = pollingEvery;
        return this;
    }

    private boolean select(CharSequence charSequence, boolean throwsExceptionWhenNotFound) {
        getInputElement().clear();

        if (charSequence == null || charSequence.equals("")) {
            return false;
        }

        getInputElement().sendKeys(charSequence);

        Wait<WebElement> wait = new FluentWait<WebElement>(getWrappedElement())
                .pollingEvery(pollingEvery)
                .withTimeout(timeout)
                .ignoring(WebDriverException.class);

        String id = getInputElement().getAttribute("id");
        String resultSelector = "div.dijitComboBoxMenuPopup[dijitpopupparent='" + id + "'] .dijitMenuItem[item]";

        try {
            return wait.until(wrappedElement -> {
                String currentText = getText();

                if (currentText == null || currentText.equals("")) {
                    getInputElement().clear();
                    getInputElement().sendKeys(charSequence);
                    return false;
                }

                List<WebElement> dropdownElements = driver.findElements(By.cssSelector(resultSelector));

                for (WebElement element : dropdownElements) {
                    if (element.isDisplayed()) {
                        String elementText = element.getText();

                        if (elementText.equals(charSequence)) {
                            element.click();
                            return true;
                        }
                    }
                }

                return false;
            });
        } catch (TimeoutException e) {
            if (throwsExceptionWhenNotFound) {
                throw new NoSuchElementException("Value '" + charSequence + "' was not found in the select list");
            }

            return false;
        }
    }
}