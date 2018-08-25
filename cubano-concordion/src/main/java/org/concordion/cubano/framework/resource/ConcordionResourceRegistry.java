package org.concordion.cubano.framework.resource;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Resource Registry.
 * <p>
 * Maintains a registry of {@link Closeable} resources at either the {@link ResourceScope#EXAMPLE},
 * {@link ResourceScope#SPECIFICATION} or {@link ResourceScope#SUITE} level. Supports closing resources individually or
 * by level, removing them from the registry on close.
 * </p>
 * This resource registry is thread-safe for SUITE scoped resources. It is not thread-safe for EXAMPLE and SPECIFICATION
 * scoped resources which rely on Concordion creating a new instance for each test.
 **/
public class ConcordionResourceRegistry implements ResourceRegistry {
    private final Deque<ImmutablePair<Closeable, CloseListener>> examplePairs = new ArrayDeque<>();
    private final Deque<ImmutablePair<Closeable, CloseListener>> specificationPairs = new ArrayDeque<>();
    private static final Deque<ImmutablePair<Closeable, CloseListener>> suitePairs = new ConcurrentLinkedDeque<>();
    private Logger logger = LoggerFactory.getLogger(ConcordionResourceRegistry.class);

    /**
     * Register a resource to be automatically closed once the fixture goes out of scope.
     * @param resource the closeable resource
     * @param scope the resource will be closed once it goes out of this scope
     */
    @Override
    public void registerCloseableResource(Closeable resource, ResourceScope scope) {
        logger.debug("Registering {} to {}.", resource, scope);
        registerResourcePair(new ImmutablePair<>(resource, null), scope);
    }

    /**
     * Register a resource to be automatically closed once the fixture goes out of scope with events published to
     * listener.
     * @param resource the closeable resource
     * @param scope the resource will be closed once it goes out of this scope
     * @param listener a listener which is called before and after closing
     */
    @Override
    public void registerCloseableResource(Closeable resource, ResourceScope scope, CloseListener listener) {
        logger.debug("Registering {} to {}.", resource, scope);
        registerResourcePair(new ImmutablePair<>(resource, listener), scope);
    }

    @Override
    public boolean isRegistered(Closeable resource, ResourceScope scope) {
        return getResourcePairFromScope(resource, scope).isPresent();
    }

    @Override
    public void closeResource(Closeable resource) {
        EnumSet.allOf(ResourceScope.class).forEach(scope -> {
            Optional<ImmutablePair<Closeable, CloseListener>> optionalPair = getResourcePairFromScope(resource, scope);
            if (optionalPair.isPresent()) {
                closeResourcePair(optionalPair.get(), scope);
            }
        });
    }

    /**
     * Close resources which are defined at {@link ResourceScope#EXAMPLE} scope.
     */
    public void closeExampleResources() {
        closeResourcePairs(examplePairs, ResourceScope.EXAMPLE);
    }

    /**
     * Close resources which are defined at {@link ResourceScope#SPECIFICATION} scope.
     */
    public void closeSpecificationResources() {
        closeResourcePairs(specificationPairs, ResourceScope.SPECIFICATION);
    }

    /**
     * Close resources which are defined at {@link ResourceScope#SUITE} scope.
     */
    public void closeSuiteResources() {
        closeResourcePairs(suitePairs, ResourceScope.SUITE);
    }

    private void registerResourcePair(ImmutablePair<Closeable, CloseListener> pair, ResourceScope scope) {
        if (pair.left == null) {
            throw new NullPointerException("Registered Resource must not be null");
        }
        switch (scope) {
            case EXAMPLE:
                examplePairs.push(pair);
                break;

            case SPECIFICATION:
                specificationPairs.push(pair);
                break;

            case SUITE:
                suitePairs.push(pair);
                break;
        }
    }

    private void closeResourcePair(ImmutablePair<Closeable, CloseListener> pair, ResourceScope scope) {
        if (pair.right != null) {
            pair.right.beforeClosing(pair.left);
        }
        try {
            logger.debug("Closing {} for scope {}.", pair.left, scope);
            pair.left.close();
        } catch (IOException e) {
            logger.warn("IOException when closing resource", e);
        }
        if (pair.right != null) {
            pair.right.afterClosing(pair.left);
        }
    }

    private void closeResourcePairs(Deque<ImmutablePair<Closeable, CloseListener>> pairs, ResourceScope scope) {
        if (pairs.isEmpty())
            return;

        logger.debug("Closing resources for {} scope.", scope);
        pairs.forEach(pair -> {
            closeResourcePair(pair, scope);
        });
        pairs.clear();
    }

    private Optional<ImmutablePair<Closeable, CloseListener>> getResourcePair(Deque<ImmutablePair<Closeable, CloseListener>> pairs, Closeable resource) {
        for (ImmutablePair<Closeable, CloseListener> pair : pairs) {
            if (pair.left.equals(resource)) {
                return Optional.of(pair);
            }
        }
        return Optional.empty();
    }

    private Optional<ImmutablePair<Closeable, CloseListener>> getResourcePairFromScope(Closeable resource, ResourceScope scope) {
        switch (scope) {
            case EXAMPLE:
                return getResourcePair(examplePairs, resource);

            case SPECIFICATION:
                return getResourcePair(specificationPairs, resource);

            case SUITE:
                return getResourcePair(suitePairs, resource);

            default:
                throw new IllegalArgumentException("Unknown scope " + scope);
        }
    }
}
