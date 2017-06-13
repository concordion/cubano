package org.concordion.cubano.driver.http;

import java.io.IOException;

/**
 * Signals a non 2xx HTTP response.
 */
public class HttpResponseException extends IOException {
    private static final long serialVersionUID = -3698376659082747423L;

    private final int statusCode;

    /**
     * Constructor.
     *
     * @param statusCode HTTP status code
     * @param message    The detail message
     */
    public HttpResponseException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

}