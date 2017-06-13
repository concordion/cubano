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

    /**
     * @return If running on a PC/MAC returns 'Desktop', otherwise returns name of the device
     */
    public String getDeviceName();

    /**
     * @return Type of device
     */
    public RemoteType getDeviceType();

    /**
     * @return Browser name if running a desktop browser, otherwise the name of the device
     */
    public String getBrowser();

    /**
     * ViewPort can mean different things on different devices:
     * <ul>
     * <li>Desktop using Applitools-Eyes: internal dimensions of the browser</li>
     * <li>Desktop: external dimensions of the browser</li>
     * <li>Device: screen resolution (information only, is not used to set browser size)</li>
     * </ul>
     *
     * @return the viewport size in format '{@literal <width>x<height>}'
     */
    public String getViewPort();

    /**
     * @return The viewport width
     */
    public int getViewPortWidth();

    /**
     * @return The viewport height
     */
    public int getViewPortHeight();

    /**
     * @return The viewport has been supplied
     */
    public boolean isViewPortDefined();

}
