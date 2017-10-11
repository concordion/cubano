package org.concordion.cubano.driver.http;

import java.io.File;
import java.io.IOException;

/**
 * File download interface.
 */
public interface HttpDownloader {

    /**
     * Download file.
     * 
     * @param url
     * @param outputFolder
     * @param cookieHeader
     * @return
     * @throws IOException
     */
    File downloadFile(String url, String outputFolder, String cookieHeader) throws IOException;
}