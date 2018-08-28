package org.concordion.cubano.driver.http;

import com.github.markusbernhardt.proxy.util.Logger.LogBackEnd;

public class ProxyVoleLoggerFactory {

    public static LogBackEnd getBackendLogger() {
        return new ProxyVoleLogger();
    }

}
