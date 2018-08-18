package org.concordion.cubano.driver.http;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.github.markusbernhardt.proxy.util.Logger.LogBackEnd;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

public class ProxyVoleLogger implements LogBackEnd {
    private static final Map<LogLevel, LoggingFunction> map;

    static {
        map = new HashMap<>();
        map.put(LogLevel.TRACE, (c, m, p) -> LoggerFactory.getLogger(c).trace(m, p));
        map.put(LogLevel.DEBUG, (c, m, p) -> LoggerFactory.getLogger(c).debug(m, p));
        map.put(LogLevel.INFO, (c, m, p) -> LoggerFactory.getLogger(c).info(m, p));
        map.put(LogLevel.WARNING, (c, m, p) -> LoggerFactory.getLogger(c).warn(m, p));
        map.put(LogLevel.ERROR, (c, m, p) -> LoggerFactory.getLogger(c).error(m, p));
    }

    @FunctionalInterface
    private interface LoggingFunction {
        public void log(Class<?> clazz, String msg, Object... params);
    }

    @Override
    public void log(Class<?> clazz, LogLevel loglevel, String msg, Object... params) {
        map.get(loglevel).log(clazz, removePositionalCharacter(msg), params);
    }

    /*
     * ProxyVole using syntax like {0} to mark parameter replacement, rename to {}
     */
    private String removePositionalCharacter(String msg) {
        return msg.replaceAll("\\{([0-9]+)\\}", "{}");
    }
}