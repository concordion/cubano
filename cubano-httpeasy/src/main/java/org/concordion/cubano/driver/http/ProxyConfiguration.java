package org.concordion.cubano.driver.http;

public enum ProxyConfiguration {
    /**
     * Proxy will be automatically determined using <a href="https://github.com/MarkusBernhardt/proxy-vole">Proxy Vole</a>
     */
    AUTOMATIC,

    /**
     * User will supply proxy configuration, if any.
     */
    MANUAL;

    public static ProxyConfiguration fromString(String name) {
        return ProxyConfiguration.valueOf(name.toUpperCase());
    }

}
