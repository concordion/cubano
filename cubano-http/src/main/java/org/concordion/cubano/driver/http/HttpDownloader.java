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
	 * @param url url of file to download
	 * @param outputFolder the output folder
	 * @param cookieHeader cookie header
	 * @return File that was downloaded
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 */
    File downloadFile(String url, String outputFolder, String cookieHeader) throws IOException;
}