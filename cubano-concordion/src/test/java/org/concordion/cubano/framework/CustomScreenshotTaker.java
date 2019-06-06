package org.concordion.cubano.framework;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;

import org.concordion.ext.ScreenshotTaker;

public class CustomScreenshotTaker implements ScreenshotTaker {

    @Override
    public Dimension writeScreenshotTo(OutputStream outputStream) throws IOException {
        return null;
    }

    @Override
    public String getFileExtension() {
        return null;
    }

}
