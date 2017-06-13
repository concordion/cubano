package org.concordion.cubano.driver.concordion;

import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.ThrowableCaughtEvent;
import org.concordion.api.listener.ThrowableCaughtListener;
import org.concordion.cubano.driver.web.Browser;
import org.concordion.cubano.driver.web.pagegrabber.GrabWebPage;
import org.concordion.ext.StoryboardExtension;
import org.concordion.ext.storyboard.CardResult;
import org.openqa.selenium.NoSuchElementException;

/**
 * Add StoryCard with link to Page HTML when NoSuchElement exception is caught.
 *
 * @author Andrew Sumner
 */
public class ExceptionHtmlCaptureExtension implements ConcordionExtension, ThrowableCaughtListener {
    private StoryboardExtension storyboard;
    private Browser browser;

    /**
     * Constructor.
     *
     * @param storyboard The storyboard
     * @param browser    The browser
     */
    public ExceptionHtmlCaptureExtension(StoryboardExtension storyboard, Browser browser) {
        this.storyboard = storyboard;
        this.browser = browser;
    }

    @Override
    public void addTo(ConcordionExtender concordionExtender) {
        concordionExtender.withThrowableListener(this);
    }

    @Override
    public void throwableCaught(ThrowableCaughtEvent event) {
        if (!browser.isOpen()) {
            return;
        }

        Throwable cause = event.getThrowable();

        do {
            if (cause instanceof NoSuchElementException) {
                WebPageContentCard card = new WebPageContentCard();
                card.setPageGrabber(new GrabWebPage(browser.getWrappedDriver()));
                card.setTitle("NoSuchElement Exception");
                card.setDescription("Click to see html");
                card.setResult(CardResult.FAILURE);

                storyboard.addCard(card);

                return;
            }

            cause = cause.getCause();
        } while (cause != null);
    }
}