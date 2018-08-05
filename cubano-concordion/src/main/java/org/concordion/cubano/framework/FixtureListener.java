package org.concordion.cubano.framework;

import org.slf4j.Logger;

public interface FixtureListener {
    void beforeExample(Class<? extends ConcordionBase> aClass, String exampleName, Logger logger);

    void afterExample(Class<? extends ConcordionBase> aClass, String exampleName, Logger logger);

    void beforeSpecification(Class<? extends ConcordionBase> aClass, Logger logger);

    void afterSpecification(Class<? extends ConcordionBase> aClass, Logger logger);

    void beforeSuite(Class<? extends ConcordionBase> aClass, Logger logger);

    void afterSuite(Class<? extends ConcordionBase> aClass, Logger logger);
}
