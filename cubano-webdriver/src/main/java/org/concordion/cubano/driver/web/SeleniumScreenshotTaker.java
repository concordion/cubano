package org.concordion.cubano.driver.web;

import java.awt.Dimension;
//import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Stack;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes screenshots of the system under test.
 *
 * @author Andrew Sumner
 */
public class SeleniumScreenshotTaker implements ScreenshotTaker {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SeleniumScreenshotTaker.class);

    protected final WebDriver driver;
    protected final WebElement element;

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
        String originalStyle = drawBorderAroundElement();

        // Selenium now takes screenshot only of the current frame, we need to go to the main document...
        Stack<WebElement> frames = cycleThroughFramesToTheParent();

        // take the screenshot...
        byte[] screenshot = takeScreenshot();

        // and end up back where we started
        cycleThroughFramesToTheChild(frames);

        outputStream.write(screenshot);

        removeBorderFromElement(originalStyle);

        return getImageDimension(screenshot);
    }

    protected byte[] takeScreenshot() {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (ClassCastException e) {
            throw new ScreenshotUnavailableException("driver does not implement TakesScreenshot");
        }

    }

    private Stack<WebElement> cycleThroughFramesToTheParent() {
        Stack<WebElement> frames = new Stack<WebElement>();

        do {

            WebElement frame = (WebElement) ((JavascriptExecutor) driver).executeScript("return window.frameElement");

            if (frame == null) {
                break;
            } else {
                frames.add(frame);
            }

            this.driver.switchTo().parentFrame();

        } while (true);

        return frames;
    }

    private void cycleThroughFramesToTheChild(Stack<WebElement> frames) {
        while (frames.size() > 0) {
            driver.switchTo().frame(frames.pop());
        }
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

    @Override
    public String getFileExtension() {
        return "png";
    }

    private static final String CHECK_IN_VIEWPORT = "var rect = arguments[0].getBoundingClientRect();" +
            "return (" +
            "rect.top >= 0 && " +
            "rect.left >= 0 && " +
            "rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */ " +
            "rect.right <= (window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */ )";

    private String drawBorderAroundElement() {
        if (element == null) {
            return "";
        }

        String originalStyle = null;

        try {
            originalStyle = element.getAttribute("style");

            // Use the wrapped driver so not logging selenium events
            JavascriptExecutor executor = ((JavascriptExecutor) driver);

            if (!(boolean) executor.executeScript(CHECK_IN_VIEWPORT, element)) {
                executor.executeScript("arguments[0].scrollIntoView(true);", element);
            }

            // Some info here on differences btwn outline and border properties
            // https://www.lifewire.com/css-outline-styles-3466217
            if (element.getTagName().equals("tr")) {
                executor.executeScript("arguments[0].style.outline='2px dashed red';", element);
            } else {
                executor.executeScript("arguments[0].style.border='2px dashed red';", element);
            }

        } catch (Exception e) {
            LOGGER.warn("Unable to set border style");
        }

        return originalStyle;
    }

    private void removeBorderFromElement(String originalStyle) {
        if (element == null) {
            return;
        }

        try {
            // Use the wrapped driver so not logging selenium events
            JavascriptExecutor executor = ((JavascriptExecutor) driver);
            if (originalStyle == null || originalStyle.isEmpty()) {
                executor.executeScript("arguments[0].removeAttribute('style')", element);
            } else {
                executor.executeScript("arguments[0].style=arguments[1]", element, originalStyle);
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to remove border style");
        }
    }
}
