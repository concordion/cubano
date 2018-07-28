package org.concordion.cubano.framework;


import org.concordion.api.option.ConcordionOptions;
import org.concordion.api.option.MarkdownExtensions;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

/**
 * Basic Concordion Fixture for inheritance by index fixtures with no tests.
 **/
@RunWith(ConcordionRunner.class)
@ConcordionOptions(markdownExtensions = {MarkdownExtensions.HARDWRAPS, MarkdownExtensions.AUTOLINKS})
public abstract class ConcordionBase {
}
