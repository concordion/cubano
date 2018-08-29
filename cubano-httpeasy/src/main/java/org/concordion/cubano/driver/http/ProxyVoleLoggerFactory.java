package org.concordion.cubano.driver.http;

import com.github.markusbernhardt.proxy.util.Logger.LogBackEnd;

public class ProxyVoleLoggerFactory {

    public static LogBackEnd getBackendLogger() {
        // Has been extracted into this method to prevent ClassNotFound exception when ProxyVole dependency has been excluded.
        // For some reason having "new ProxyVoleLogger()" in HttpEasyDefaults class caused problems at runtime.
        return new ProxyVoleLogger();
    }

}
