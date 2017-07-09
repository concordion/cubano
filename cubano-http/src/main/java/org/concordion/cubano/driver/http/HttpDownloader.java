package org.concordion.cubano.driver.http;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface HttpDownloader {
    File downloadFile(String url, String outputFolder, String cookieHeader) throws IOException;
}