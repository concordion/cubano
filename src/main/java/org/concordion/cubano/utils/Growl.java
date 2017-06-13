package org.concordion.cubano.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Use jQuery Growl to display messages in the browser.
 * <p>
 * <ul>
 * <li>This has not been tried out yet</li>
 * <li>It will need some work to support running behind a proxy</li>
 * </ul>
 * <p>
 * See: http://ksylvest.github.io/jquery-growl
 *
 * @author Andrew Sumner
 */
public class Growl {
    private final JavascriptExecutor js;

    /**
     * Constructor.
     *
     * @param driver WebDriver
     */
    public Growl(WebDriver driver) {
        this.js = (JavascriptExecutor) driver;
    }

    private void init() {
        // Check for jQuery on the page, add it if need be
        js.executeScript("if (!window.jQuery) {" +
                "var jquery = document.createElement('script'); jquery.type = 'text/javascript';" +
                "jquery.src = 'https://ajax.googleapis.com/ajax/libs/jquery/2.0.2/jquery.min.js';" +
                "document.getElementsByTagName('head')[0].appendChild(jquery);" +
                "}");

        // Use jQuery to add jquery-growl to the page
        js.executeScript("$.getScript('http://the-internet.herokuapp.com/js/vendor/jquery.growl.js')");

        // Use jQuery to add jquery-growl styles to the page
        js.executeScript("$('head').append('<link rel=\"stylesheet\" href=\"http://the-internet.herokuapp.com/css/jquery.growl.css\" type=\"text/css\" />');");
    }

    /**
     * No frills growl.
     *
     * @param title   Title to display
     * @param message Message to display
     */
    public void show(String title, String message) {
        init();

        js.executeScript(String.format("$.growl({ title: '%s', message: '%s' });", title, message));
    }

    /**
     * Error message.
     *
     * @param message Message to display
     */
    public void error(String message) {
        init();

        js.executeScript(String.format("$.growl.error({ title: 'ERROR', message: '%s' });", message));
    }

    /**
     * Notice message.
     *
     * @param message Message to display
     */
    public void notice(String message) {
        init();

        js.executeScript(String.format("$.growl.notice({ title: 'Notice', message: '%s' });", message));
    }

    /**
     * Warning message.
     *
     * @param message Message to display
     */
    public void warning(String message) {
        init();

        js.executeScript(String.format("$.growl.warning({ title: 'Warning!', message: '%s' });", message));
    }
}
