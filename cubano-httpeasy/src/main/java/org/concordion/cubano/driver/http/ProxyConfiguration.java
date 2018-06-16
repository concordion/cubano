package org.concordion.cubano.driver.http;

public enum ProxyConfiguration {
    AUTOMATIC, MANUAL;

    public static ProxyConfiguration fromString(String name) {
        return ProxyConfiguration.valueOf(name.toUpperCase());
    }

}
