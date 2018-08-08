package org.concordion.cubano.framework.resource;

import java.io.Closeable;

public interface ResourceRegistry {
    void registerCloseableResource(Closeable resource, ResourceScope scope);

    void registerCloseableResource(Closeable resource, ResourceScope scope, CloseListener listener);
}
