package org.concordion.cubano.driver.http.logging;

/**
 * An interface for proving custom logging output.
 */
public abstract class LogWriter {

    public abstract void info(String msg, Object... args);
    
    public abstract void request(String msg, Object... args);

    public abstract void response(String msg, Object... args);

    public abstract void error(String message, Throwable t);

    protected String getFormattedMessage(String message, Object... args) {
        String msg = message.replace("{}", "%s");
        return String.format(msg, args);
    }

}
