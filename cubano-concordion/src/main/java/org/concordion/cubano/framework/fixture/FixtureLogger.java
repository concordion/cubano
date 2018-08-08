package org.concordion.cubano.framework.fixture;

import org.concordion.cubano.framework.ConcordionBase;
import org.concordion.cubano.framework.fixture.FixtureListener;
import org.slf4j.Logger;

public class FixtureLogger implements FixtureListener {
    @Override
    public void beforeExample(Class<? extends ConcordionBase> aClass, String exampleName, Logger logger) {
    }

    @Override
    public void afterExample(Class<? extends ConcordionBase> aClass, String exampleName, Logger logger) {
    }

    @Override
    public void beforeSpecification(Class<? extends ConcordionBase> aClass, Logger logger) {
    }

    @Override
    public void afterSpecification(Class<? extends ConcordionBase> aClass, Logger logger) {
        logger.info("Tearing down the acceptance test class {} on thread {}",
                aClass.getSimpleName(), Thread.currentThread().getName());
    }

    @Override
    public void beforeSuite(Class<? extends ConcordionBase> aClass, Logger logger) {
    }

    @Override
    public void afterSuite(Class<? extends ConcordionBase> aClass, Logger logger) {
        logger.info("Tearing down the test suite (called from test class {} on thread {}). ",
                aClass.getSimpleName(), Thread.currentThread().getName());
    }
}
