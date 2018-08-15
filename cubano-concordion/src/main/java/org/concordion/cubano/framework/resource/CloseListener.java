package org.concordion.cubano.framework.resource;

import java.io.Closeable;

public interface CloseListener {
    default void beforeClosing(Closeable resource) {};
    default void afterClosing(Closeable resource) {};
}
