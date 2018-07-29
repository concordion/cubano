package org.concordion.cubano.driver.web.provider;

import org.openqa.selenium.WebDriver;

import java.io.Closeable;

/**
 * Interface for information required to start a browser locally or remotely.
 *
 * Invoking the {@link #close()} method will kill any left over driver instances.
 */
public interface BrowserProvider extends Closeable {
    /**
     * @return A new Selenium WebDriver based on supplied configuration
     */
    WebDriver createDriver();
}
