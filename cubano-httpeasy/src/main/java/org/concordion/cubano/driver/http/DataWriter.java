package org.concordion.cubano.driver.http;

import java.io.IOException;

import org.slf4j.Logger;

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
    public void write(Logger logger) throws IOException;

}
