package org.concordion.cubano.driver.http;

/**
 * An interface for proving custom logging output.
 *
 * @author Andrew Sumner
 */
public interface LogWriter {

    /**
     * Write info level log message.
     *
     * @param message Log message
     */
    public void info(String message);
}
