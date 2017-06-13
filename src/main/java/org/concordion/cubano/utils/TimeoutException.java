package org.concordion.cubano.utils;

/**
 * Timeout Exception.
 */
public class TimeoutException extends RuntimeException {
    private static final long serialVersionUID = -4951269355032677631L;

    /**
     * Constructor.
     *
     * @param message The detail message
     */
    public TimeoutException(final String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message The detail message
     * @param cause   The cause
     */
    public TimeoutException(final String message, Throwable cause) {
        super(message, cause);
    }
}
