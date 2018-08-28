package org.concordion.cubano.framework;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.concordion.api.*;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.api.option.MarkdownExtensions;
import org.concordion.cubano.framework.fixture.FixtureListener;
import org.concordion.cubano.framework.fixture.FixtureLogger;
import org.concordion.cubano.framework.resource.CloseListener;
import org.concordion.cubano.framework.resource.ResourceRegistry;
import org.concordion.cubano.framework.resource.ResourceScope;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
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
 * Basic Concordion Fixture for inheritance by index fixtures with no tests.
 * <p>
 * Supports the automatic closing of {@link Closeable} resources at either the {@link ResourceScope#SPECIFICATION}
 * or {@link ResourceScope#SUITE} level. After calling {@link #registerCloseableResource(Closeable, ResourceScope)}
 * , the resource will automatically be closed at the end of the specified scope.
 * Resources will be closed in the reverse order to which they were registered.
 * </p>
 * The resource registry is thread-safe for SUITE scoped resources. It is not thread-safe for EXAMPLE and SPECIFICATION
 * scoped resources which rely on Concordion creating a new instance for each test.
 **/
@RunWith(ConcordionRunner.class)
@ConcordionOptions(markdownExtensions = {MarkdownExtensions.HARDWRAPS, MarkdownExtensions.AUTOLINKS})
public abstract class ConcordionBase implements ResourceRegistry {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected FixtureListener fixtureListener = new FixtureLogger();

    private final Deque<ImmutablePair<Closeable, CloseListener>> examplePairs = new ArrayDeque<>();
    private final Deque<ImmutablePair<Closeable, CloseListener>> specificationPairs = new ArrayDeque<>();
    private static final Deque<ImmutablePair<Closeable, CloseListener>> suitePairs = new ConcurrentLinkedDeque<>();

    /**
     * Replace the FixtureLogger with a different FixtureListener.
     *
     * @param fixtureListener the fixture listener to use
     */
    protected void withFixtureListener(FixtureListener fixtureListener) {
        this.fixtureListener = fixtureListener;
    }

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

    @BeforeExample
    private void actionBeforeExample(@ExampleName String exampleName) {
        fixtureListener.beforeExample(this.getClass(), exampleName, logger);
    }

    @AfterExample
    private void actionAfterExample(@ExampleName String exampleName) {
        fixtureListener.afterExample(this.getClass(), exampleName, logger);
    }

    @BeforeSpecification
    private void actionBeforeSpecification() {
        fixtureListener.beforeSpecification(this.getClass(), logger);
    }

    @AfterSpecification
    private void actionAfterSpecification() {
        fixtureListener.afterSpecification(this.getClass(), logger);
    }

    @BeforeSuite
    private void actionBeforeSuite() {
        fixtureListener.beforeSuite(this.getClass(), logger);
    }

    @AfterSuite
    private void actionAfterSuite() {
        fixtureListener.afterSuite(this.getClass(), logger);
    }
    /**
     * Close resources which are defined at {@link ResourceScope#EXAMPLE} scope.
     */
    @AfterExample
    protected void closeExampleResources() {
        closeResourcePairs(examplePairs, ResourceScope.EXAMPLE);
    }

    /**
     * Close resources which are defined at {@link ResourceScope#SPECIFICATION} scope.
     */
    @AfterSpecification
    protected void closeSpecificationResources() {
        closeResourcePairs(specificationPairs, ResourceScope.SPECIFICATION);
    }

    /**
     * Close resources which are defined at {@link ResourceScope#SUITE} scope.
     */
    @AfterSuite
    protected void closeSuiteResources() {
        closeResourcePairs(suitePairs, ResourceScope.SUITE);
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

    private Optional<ImmutablePair<Closeable, CloseListener>> getResourcePair(Deque<ImmutablePair<Closeable, CloseListener>> pairs, Closeable resource) {
        for (ImmutablePair<Closeable, CloseListener> pair : pairs) {
            if (pair.left.equals(resource)) {
                return Optional.of(pair);
            }
        }
        return Optional.empty();
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
}
