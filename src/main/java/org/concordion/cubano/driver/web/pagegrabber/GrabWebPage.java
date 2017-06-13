package org.concordion.cubano.driver.web.pagegrabber;

import java.io.File;
import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This program will grab the whole web page including all its images,
 * css and js files and stores them in a single output directory with
 * all urls in html page modified to point to this directory itself.
 *
 * @author Pramod Khare
 * @author Andrew Sumner
 */
public class GrabWebPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrabWebPage.class);
    private WebDriver driver;

    /**
     * Constructor.
     *
     * @param browser Browser driver
     */
    public GrabWebPage(WebDriver browser) {
        this.driver = browser;
    }


    /**
     * Save the page source and all linked resources that reside on the same host.
     *
     * @param outputDirPath Destination folder
     * @param fileName      Name of HTML file
     * @throws Exception
     */
    public void getWebPage(String outputDirPath, String fileName) throws Exception {
        File outputDir = new File(outputDirPath);

        try {
            if (outputDir.exists() && outputDir.isFile()) {
                throw new RuntimeException("output directory path is wrong, please provide some directory path");
            } else if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    throw new IOException("Unable to create the folder " + outputDir.getPath());
                }
            }

            // Output file name
            File outputFile = new File(outputDir, fileName);

            // if file doesn't exists, then create it
            if (!outputFile.exists()) {
                if (!outputFile.createNewFile()) {
                    throw new IOException("Unable to create the file " + outputFile.getPath());
                }
            }

            // can we write this file
            if (!outputFile.canWrite()) {
                throw new IOException("Cannot write to file - " + outputFile.getAbsolutePath());
            }

            LOGGER.debug("Downloading WebPage {}", driver.getCurrentUrl());
            new GrabUtility(driver).savePageTo(outputDirPath, fileName);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }
}