package org.concordion.cubano.driver.http;

/**
 * An enumeration representing the class of an http response status code.
 */
public enum Family {

    /**
     * {@code 1xx} HTTP status codes.
     */
    INFORMATIONAL,
    /**
     * {@code 2xx} HTTP status codes.
     */
    SUCCESSFUL,
    /**
     * {@code 3xx} HTTP status codes.
     */
    REDIRECTION,
    /**
     * {@code 4xx} HTTP status codes.
     */
    CLIENT_ERROR,
    /**
     * {@code 5xx} HTTP status codes.
     */
    SERVER_ERROR,
    /**
     * Other, unrecognized HTTP status codes.
     */
    OTHER;

    /**
     * Get the response status family for the status code.
     *
     * @param statusCode response status code to get the family for.
     * @return family of the response status code.
     */
    public static Family familyOf(final int statusCode) {
        switch (statusCode / 100) {
            case 1:
                return Family.INFORMATIONAL;
            case 2:
                return Family.SUCCESSFUL;
            case 3:
                return Family.REDIRECTION;
            case 4:
                return Family.CLIENT_ERROR;
            case 5:
                return Family.SERVER_ERROR;
            default:
                return Family.OTHER;
        }
    }
}