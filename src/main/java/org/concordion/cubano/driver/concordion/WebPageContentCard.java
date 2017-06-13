package org.concordion.cubano.driver.concordion;

import java.io.File;

import org.concordion.api.Element;
import org.concordion.api.Resource;
import org.concordion.cubano.driver.web.pagegrabber.GrabWebPage;
import org.concordion.ext.storyboard.Card;
import org.concordion.ext.storyboard.CardImage;
import org.concordion.ext.storyboard.StockCardImage;
import org.concordion.internal.ConcordionBuilder;

/**
 * Downloads HTML etc for page under test for offline analysis.
 *
 * @author Andrew Sumner
 */
public class WebPageContentCard extends Card {
    private GrabWebPage pageGrabber;
    private String dataFileName = "";
    private CardImage cardImage = StockCardImage.HTML;

    protected void setPageGrabber(final GrabWebPage pageGrabber) {
        this.pageGrabber = pageGrabber;
    }

    @Override
    protected void captureData() {
        dataFileName = getFileName(getResource().getName(), getItemIndex(), "html");
        dataFileName = dataFileName.substring(0, dataFileName.length() - 5) + "/NoSuchElement.html";

        Resource resource = getResource().getRelativeResource(dataFileName);
        File file = new File(ConcordionBuilder.getBaseOutputDir(), resource.getPath());

        try {
            pageGrabber.getWebPage(file.getParent(), file.getName());
        } catch (Exception e) {
            // Unable to write file
            this.dataFileName = "";
        }
    }

    @Override
    protected void addHTMLToContainer(final Element container) {
        String imageName = getResource().getRelativePath(cardImage.getResource());

        Element img = new Element("img");
        img.setId(this.getDescription());
        img.addStyleClass("sizeheight");
        img.addAttribute("src", imageName);

        if (dataFileName.isEmpty()) {
            container.appendChild(img);
        } else {
            Element anchorImg = new Element("a");
            anchorImg.addAttribute("href", dataFileName);
            container.appendChild(anchorImg);

            anchorImg.appendChild(img);
        }
    }
}
