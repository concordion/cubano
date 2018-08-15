package org.concordion.cubano.driver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerLogWriter extends LogWriter {
    static final Logger LOGGER = LoggerFactory.getLogger(HttpEasy.class);
   
    @Override
    public void info(String msg, Object... args) {
        LOGGER.debug(msg, args);
    }

    @Override
    public void request(String msg, Object... args) {
        LOGGER.trace(msg, args);
    }
    
    @Override
    public void response(String msg, Object... args) {
        LOGGER.trace(msg, args);
    }

    @Override
    public void error(String message, Throwable t) {
        LOGGER.error(message, t);
    }
}
