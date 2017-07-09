package org.concordion.cubano.driver.http;

/**
 * Interface for common response reader methods.
 */
public interface ResponseReader {

    /**
     * @return The underlying data (eg XML or JSON document) as a nicely formatted string.
     */
    public String asPrettyString() throws Exception;
}
