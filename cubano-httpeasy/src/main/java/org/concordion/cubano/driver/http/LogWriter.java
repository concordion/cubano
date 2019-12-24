package org.concordion.cubano.driver.http;

import java.text.MessageFormat;

/**
 * An interface for proving custom logging output.
 */
public abstract class LogWriter {

    public abstract void info(String msg, Object... args);
    
    public abstract void request(String msg, Object... args);

    public abstract void response(String msg, Object... args);

    public abstract void error(String message, Throwable t);

    /**
     * Uses MessageFormat to create string
     */
    protected String getFormattedMessage(String message, Object... args) {
        return MessageFormat.format(message, args);
    }

}
