package org.concordion.cubano.driver.http;

import java.io.File;
import java.io.IOException;

public class HttpEasyDownloader implements HttpDownloader {
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