package demo;

import org.concordion.api.AfterSpecification;
import org.concordion.api.extension.Extension;
import org.concordion.cubano.driver.web.Browser;
import org.concordion.ext.LoggingFormatterExtension;
import org.concordion.ext.StoryboardExtension;
import org.concordion.ext.StoryboardLogListener;
import org.concordion.ext.StoryboardMarker;
import org.concordion.ext.storyboard.CardResult;
import org.concordion.ext.storyboard.StockCardImage;
import org.concordion.integration.junit4.ConcordionRunner;
import org.concordion.slf4j.ext.MediaType;
import org.concordion.slf4j.ext.ReportLogger;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.junit.Assert;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class HelloWorldFixture {

    @Extension
    StoryboardExtension storyboard = new StoryboardExtension();

    @Extension
    LoggingFormatterExtension logging = new LoggingFormatterExtension().registerListener(new StoryboardLogListener(storyboard));

    static Browser browser = new Browser();

    ReportLogger logger = ReportLoggerFactory.getReportLogger(HelloWorldFixture.class);

    public String getGreetingFailure() {
        browser.getDriver().navigate().to("http://google.co.nz");

        logger.with()
                .message("Hello World!")
                .attachment("This is some data", "data.txt", MediaType.PLAIN_TEXT)
                .marker(new StoryboardMarker("Hello", "Data", StockCardImage.TEXT, CardResult.SUCCESS))
                .debug();

        return "Failed";
    }

    public String getGreetingException() {
        browser.getDriver().navigate().to("http://google.co.nz");

        Assert.fail("Frogs legs");

        return "Failed";
    }

    @AfterSpecification
    public void afterSpecification() {
        browser.close();
    }
}
