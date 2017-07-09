package org.concordion.cubano.driver;

import org.concordion.cubano.driver.web.Browser;

/**
 * Interface that tests using the framework should implement .
 *
 * @author Andrew Sumner
 */
public interface BrowserBasedTest {

    /**
     * @return Reference to the Browser.
     */
    public Browser getBrowser();
}
