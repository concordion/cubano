package org.concordion.cubano.framework;

import org.concordion.api.extension.Extension;
import org.concordion.ext.LoggingFormatterExtension;
import org.concordion.ext.StoryboardExtension;
import org.concordion.ext.StoryboardLogListener;
import org.concordion.logback.LogbackAdaptor;

/**
 * Concordion fixture for inheritance by any test classes.
 * Includes and configures the Storyboard and Logging extensions.
 **/
public abstract class ConcordionFixture extends ConcordionBase {
    @Extension
    private final StoryboardExtension storyboard = new StoryboardExtension();

    @Extension
    private final LoggingFormatterExtension loggerExtension = new LoggingFormatterExtension()
            .registerListener(new StoryboardLogListener(getStoryboard()));

    protected StoryboardExtension getStoryboard() {
        return storyboard;
    }

    static {
        LogbackAdaptor.logInternalStatus();
    }
}
