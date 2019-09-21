package org.concordion.cubano.driver.http.dataWriter;

import java.io.IOException;

import org.concordion.cubano.driver.http.logging.LogManager;

/**
 * Http request data writer interface.
 *
 * @author Andrew Sumner
 */
public interface DataWriter {

    /**
     * Add data to Http request.
     *
     * @param logger Logger to write details to
     * @throws IOException If unable to read the response
     */
    public void write(LogManager logger) throws IOException;

}
