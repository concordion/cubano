package org.concordion.cubano.driver.web.provider;

/**
 * Provide a few session details from a remote browser session (ie Selenium Grid).
 *
 * @author Andrew Sumner
 */
public class SessionDetails {
    private String browserUrl;
    private String videoUrl;
    private String providerName;

    public String getBrowserUrl() {
        return browserUrl;
    }

    public void setBrowserUrl(String browserUrl) {
        this.browserUrl = browserUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}