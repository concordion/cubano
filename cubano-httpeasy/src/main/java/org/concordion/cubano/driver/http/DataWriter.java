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
     * @throws IOException
     */
    public void write(Logger logger) throws IOException;

}
