package org.concordion.cubano.driver.web.pagefactory;

import org.concordion.cubano.driver.web.BasePageObject;

/**
 * Allows page components to perform navigation and other tasks and still use all the framework features .
 * <p>
 * <p><pre>
 * public class SomeComponent extends HtmlElement implements PageObjectAware {
 *     private WebHelper webHelper;
 *     private WebElement button;
 * <p>
 *     {@literal @Override}
 *     public void setPageObject(BasePageObject<?> pageObject) {
 *         this.webHelper = new WebHelper(pageObject);
 *     }
 * <p>
 *     public void clickButton() {
 *         webHelper.navigateUsing(button);
 *     }
 * }
 * </pre></p>
 *
 * @author Andrew Sumner
 */
public interface PageObjectAware {

    /**
     * Sets the page object for the implementing class.
     *
     * @param pageObject PageObject
     */
    public void setPageObject(BasePageObject<?> pageObject);
}
