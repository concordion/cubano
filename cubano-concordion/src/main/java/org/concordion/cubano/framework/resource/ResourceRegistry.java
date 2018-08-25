package org.concordion.cubano.framework.resource;

import java.io.Closeable;

/**
 * Register {@link Closeable} resources to be automatically closed at either the {@link ResourceScope#EXAMPLE},
 * {@link ResourceScope#SPECIFICATION} or {@link ResourceScope#SUITE} level.
 * Resources will be closed in the reverse order to which they were registered.
 */
public interface ResourceRegistry {
    /**
     * Automatically close the <code>resource</code> at the end of the specified <code>scope</code>.
     */
    void registerCloseableResource(Closeable resource, ResourceScope scope);

    /**
     * Automatically close the <code>resource</code> at the end of the specified <code>scope</code>. Call the
     * relevant methods on <code>listener</code> before and after closing the resource.
     */
    void registerCloseableResource(Closeable resource, ResourceScope scope, CloseListener listener);

    /**
     * Ascertains whether the <code>resource</code> is registered at the specified <code>scope</code>.
     */
    boolean isRegistered(Closeable resource, ResourceScope scope);

    /**
     * Close and deregister the specified <code>resource</code> from all scopes.
     */
    void closeResource(Closeable resource);
}
