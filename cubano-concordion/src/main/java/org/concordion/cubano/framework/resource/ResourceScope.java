package org.concordion.cubano.framework.resource;

/**
 * The scope to be used for closing resources.
 */
public enum ResourceScope {
    /**
     * Per Concordion Specification Example
     */
    EXAMPLE,
    /**
     * Per Concordion Specification
     */
    SPECIFICATION,
    /**
     * Per Concordion Suite, where a Suite equates to the top-level test that is invoked (normally using JUnit).
     * Other tests that are invoked from the top-level test using the Run Command are part of the suite.
     * Other tests invoked directly (from JUnit) form independent suites.
     */
    SUITE;
}
