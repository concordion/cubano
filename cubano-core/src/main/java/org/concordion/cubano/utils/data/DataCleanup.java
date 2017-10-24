package org.concordion.cubano.utils.data;

/**
 * Interface for those classes wish to automatically cleanup data, for example data added to database for a test run.
 *
 * @author Andrew Sumner
 */
public interface DataCleanup {

    /**
     * Cleanup data.
     */
    public void cleanup();
}
