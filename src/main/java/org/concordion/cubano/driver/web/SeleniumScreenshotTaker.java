package org.concordion.cubano.driver.web;

import java.awt.Dimension;
//import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.concordion.ext.ScreenshotTaker;
import org.concordion.ext.ScreenshotUnavailableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
//import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringWebDriver;

/**
 * Takes screenshots of the system under test.
 *
 * @author Andrew Sumner
 */
public class SeleniumScreenshotTaker implements ScreenshotTaker {

    private final WebDriver driver;
    private final WebElement element;

    /**
     * Constructor.
     *
     * @param driver Driver for the browser to take screenshots of
     */
    public SeleniumScreenshotTaker(WebDriver driver) {
        this.driver = getBaseDriver(driver);
        this.element = null;
    }

    /**
     * Constructor.
     *
     * @param driver  Driver for the browser to take screenshots of
     * @param element Element to either highlight or take screenshot of rather than entire page
     */
    public SeleniumScreenshotTaker(WebDriver driver, WebElement element) {
        this.driver = getBaseDriver(driver);
        this.element = element;
    }

    private WebDriver getBaseDriver(WebDriver driver) {
        WebDriver baseDriver = driver;

        while (baseDriver instanceof EventFiringWebDriver) {
            baseDriver = ((EventFiringWebDriver) baseDriver).getWrappedDriver();
        }

        return baseDriver;
    }

    @Override
    public Dimension writeScreenshotTo(OutputStream outputStream) throws IOException {
        // TODO Support screenshot of element rather than full page

        // Unfortunately both Ashot and webdriver approaches are not taking iframes into account and are coming up with the
        // wrong location. I'd also be interested in getting a new AShot ShootingStrategy similar to ViewportPastingDecorator that
        // works on scrolling web elements and take a 'full' element screenshot by stitching images together

        // Screenshot screenshot;
        //
        // try {
        // if (element == null) {
        // screenshot = new AShot()
        // .shootingStrategy(ShootingStrategies.viewportPasting(100))
        // .takeScreenshot(driver);
        // } else {
        // // set custom cropper with indentation and blur filter for indented areas
        // screenshot = new AShot()
        // .coordsProvider(new WebDriverCoordsProvider())
        // .imageCropper(new IndentCropper().addIndentFilter(new BlurFilter()))
        // .takeScreenshot(driver, element);
        // }
        //
        // ImageIO.write(screenshot.getImage(), "png", outputStream);
        //
        // return new Dimension(screenshot.getImage().getWidth(), screenshot.getImage().getHeight());
        // } catch (Exception e) {
        // return new Dimension(0, 0);
        // }

        String originalStyle = drawBorderAroundElement();

        byte[] screenshot;
        try {
            screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (ClassCastException e) {
            throw new ScreenshotUnavailableException("driver does not implement TakesScreenshot");
        }

        // if (element == null) {
        outputStream.write(screenshot);

        removeBorderFromElement(originalStyle);

        return getImageDimension(screenshot);
        // } else {
        // int imageWidth = element.getSize().getWidth();
        // int imageHeight = element.getSize().getHeight();
        // Point point = element.getLocation();
        // int xcord = point.getX();
        // int ycord = point.getY();
        //
        // BufferedImage bi = getImage(screenshot);
        // BufferedImage dest = bi.getSubimage(xcord, ycord, imageWidth, imageHeight);
        // ImageIO.write(dest, "png", outputStream);
        //
        // return new Dimension(dest.getWidth(), dest.getHeight());
        // }
    }

    private Dimension getImageDimension(byte[] screenshot) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(screenshot))) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);

                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        }

        throw new RuntimeException("Unable to read image dimensions");
    }


    // private BufferedImage getImage(byte[] screenshot) throws IOException {
    // try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(screenshot))) {
    // final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
    // if (readers.hasNext()) {
    // ImageReader reader = readers.next();
    // try {
    // reader.setInput(in);
    //
    // return reader.read(0);
    //
    // // return new Dimension(reader.getWidth(0), reader.getHeight(0));
    // } finally {
    // reader.dispose();
    // }
    // }
    // }
    //
    // throw new RuntimeException("Unable to read image dimensions");
    // }

    @Override
    public String getFileExtension() {
        return "png";
    }

    private static final String CHECK_IN_VEWPORT = "var rect = arguments[0].getBoundingClientRect();" +
            "return (" +
            "rect.top >= 0 && " +
            "rect.left >= 0 && " +
            "rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */ " +
            "rect.right <= (window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */ )";

    private String drawBorderAroundElement() {
        if (element == null) {
            return "";
        }

        String originalStyle = element.getAttribute("style");

        // Use the wrapped driver so not logging selenium events
        JavascriptExecutor executor = ((JavascriptExecutor) driver);

        if (!(boolean) executor.executeScript(CHECK_IN_VEWPORT, element)) {
            executor.executeScript("arguments[0].scrollIntoView(true);", element);
        }

        // executor.executeScript("arguments[0].style.outline='2px dashed red'; arguments[0].style.outlineOffset='1px';", element);
        executor.executeScript("arguments[0].style.border='2px dashed red';", element);

        return originalStyle;
    }

    private void removeBorderFromElement(String originalStyle) {
        if (element == null) {
            return;
        }

        // Use the wrapped driver so not logging selenium events
        JavascriptExecutor executor = ((JavascriptExecutor) driver);
        if (originalStyle == null || originalStyle.isEmpty()) {
            executor.executeScript("arguments[0].removeAttribute('style')", element);
        } else {
            executor.executeScript("arguments[0].style=arguments[1]", element, originalStyle);
        }
    }
}
