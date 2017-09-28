package org.concordion.cubano.driver.web.provider;

import org.openqa.selenium.WebDriver;

/**
 * Interface for information required to start a browser locally or remotely.
 *
 * @author Andrew Sumner
 */
public interface BrowserProvider {
    /**
     * @return A new Selenium WebDriver based on supplied configuration
     */
    public WebDriver createDriver();

}
