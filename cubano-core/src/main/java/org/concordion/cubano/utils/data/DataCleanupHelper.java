package org.concordion.cubano.utils.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Registration service for data cleanup.
 *
 * @author Andrew Sumner
 */
public class DataCleanupHelper {
    private List<DataCleanup> toClean = new ArrayList<DataCleanup>();

    /**
     * Register class implementing DataCleanup interface.
     *
     * @param toclean Class wishing to cleanup data
     */
    public void register(DataCleanup toclean) {
        this.toClean.add(toclean);
    }

    /**
     * Cleanup data from any registered classes.
     */
    public void cleanup() {
        for (DataCleanup data : toClean) {
            data.cleanup();
        }

        toClean.clear();
    }

    /**
     * @return True if has registered classes.
     */
    public boolean hasCleanupItems() {
        return toClean.size() > 0;
    }
}
