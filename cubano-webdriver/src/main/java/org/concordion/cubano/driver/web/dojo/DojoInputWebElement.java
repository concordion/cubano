package org.concordion.cubano.driver.web.dojo;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ru.yandex.qatools.htmlelements.element.HtmlElement;

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
public class DojoInputWebElement extends HtmlElement {
    @FindBy(xpath = ".//input[contains(@class, 'dijitInputInner')]")
    WebElement input;

    public WebElement getInput() {
        return input;
    }

    @Override
    public String getText() {
        return input.getAttribute("value");
    }

    @Override
    public void sendKeys(CharSequence... charSequences) {
        input.sendKeys(charSequences);
    }

    @Override
    public void clear() {
        input.clear();
    }

    @Override
    public void click() {
        input.click();
    }
}
