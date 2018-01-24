package demo;

import org.concordion.api.extension.Extension;
import org.concordion.cubano.framework.ConcordionBase;
import org.concordion.ext.LoggingFormatterExtension;
import org.concordion.ext.StoryboardExtension;
import org.concordion.ext.StoryboardLogListener;

/**
 * Project customisation layer.
 */
public class ConcordionTempBase extends ConcordionBase {
    @Extension
    private final StoryboardExtension storyboard = new StoryboardExtension();

    @Extension
    private final LoggingFormatterExtension loggerExtension = new LoggingFormatterExtension()
            .registerListener(new StoryboardLogListener(getStoryboard()));

    /**
     * @return A reference to the Storyboard extension.
     */
    protected StoryboardExtension getStoryboard() {
        return storyboard;
    }
}
