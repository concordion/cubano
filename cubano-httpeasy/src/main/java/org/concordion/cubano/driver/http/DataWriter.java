package org.concordion.cubano.driver.http;

import java.io.IOException;

/**
 * Http request data writer interface.
 *
 * @author Andrew Sumner
 */
interface DataWriter {

    /**
     * Add data to Http request.
     *
     * @param logger Logger to write details to
     * @throws IOException If unable to read the response
     */
    public void write(LogManager logger) throws IOException;

}
