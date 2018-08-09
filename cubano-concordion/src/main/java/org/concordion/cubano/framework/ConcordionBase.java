package org.concordion.cubano.framework;


import org.concordion.api.*;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.api.option.MarkdownExtensions;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Basic Concordion Fixture for inheritance by index fixtures with no tests.
 * <p>
 * Supports the automatic closing of {@link Closeable} resources at either the {@link ResourceScope#SPECIFICATION}
 * or {@link ResourceScope#SUITE} level. After calling {@link #registerCloseableResource(Closeable, ResourceScope)}
 * , the resource will automatically be closed at the end of the specified scope.
 * Resources will be closed in the reverse order to which they were registered.
 * </p>
 **/
@RunWith(ConcordionRunner.class)
@ConcordionOptions(markdownExtensions = {MarkdownExtensions.HARDWRAPS, MarkdownExtensions.AUTOLINKS})
public abstract class ConcordionBase {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected FixtureListener fixtureListener = new FixtureLogger();

    private final Deque<Closeable> specificationResources = new ArrayDeque<>();
    private static final Deque<Closeable> suiteResources = new ConcurrentLinkedDeque<>();

    /**
     * Register a resource to be automatically invoked once the fixture goes out of scope.
     * @param resource the closeable resource
     * @param scope the resource will be closed once it goes out of this scope
     */
    protected void registerCloseableResource(Closeable resource, ResourceScope scope) {
        logger.debug("Registering {} to {}.", resource, scope);
        if (scope.equals(ResourceScope.SPECIFICATION)) {
            specificationResources.push(resource);
        } else if (scope.equals(ResourceScope.SUITE)) {
            suiteResources.push(resource);
        } else {
            throw new IllegalArgumentException("Unknown scope '" + scope + "'");
        }
    }

    protected boolean isRegistered(Closeable resource, ResourceScope scope) {
        if (scope.equals(ResourceScope.SPECIFICATION)) {
            return specificationResources.contains(resource);
        } else if (scope.equals(ResourceScope.SUITE)) {
            return suiteResources.contains(resource);
        } else {
            throw new IllegalArgumentException("Unknown scope '" + scope + "'");
        }
    }

    /**
     * Close resources which are defined at {@link org.concordion.cubano.framework.ResourceScope#SPECIFICATION} scope.
     */
    @AfterSpecification
    protected void closeSpecificationResources() {
        logger.debug("Closing specification resources {}.", specificationResources);
        closeResources(specificationResources);
    }

    /**
     * Close resources which are defined at {@link org.concordion.cubano.framework.ResourceScope#SUITE} scope.
     */
    @AfterSuite
    protected void closeSuiteResources() {
        logger.debug("Closing suite resources {}.", suiteResources);
        closeResources(suiteResources);
    }

    private void closeResources(Deque<Closeable> resources) {
        resources.forEach(resource -> {
            try {
                logger.debug("Closing {}.", resource);
                resource.close();
            } catch (IOException e) {
                logger.warn("IOException when closing resource", e);
            }
        });
        resources.clear();
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
     * Replace the FixtureLogger with a different FixtureListener.
     *
     * @param fixtureListener the fixture listener to use
     */
    protected void withFixtureListener(FixtureListener fixtureListener) {
        this.fixtureListener = fixtureListener;
    }
}
