package org.concordion.cubano.driver.concordion;

import java.io.File;
import java.io.IOException;

import org.concordion.cubano.driver.http.HttpDownloader;
import org.concordion.cubano.driver.http.HttpEasy;

/**
 * File downloader.
 */
public class HttpEasyDownloader implements HttpDownloader {

    /**
     * Download file.
     */
    public File downloadFile(String url, String outputFolder, String cookieHeader) throws IOException {
        return HttpEasy.request().
                header("Accept-Language", "en-US,en;q=0.8").
                header("User-Agent", "Java").
                header("Referer", "google.com").
                header("Cookie", cookieHeader).
                path(url).
                get().
                downloadFile(outputFolder);	
    }
}