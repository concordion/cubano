package org.concordion.cubano.utils.data;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.concordion.cubano.utils.ActionWait;

/**
 * Maintains a global pool of of available entities and those that are in use.
 * <p>
 * <p>
 * request() and release() methods: It is expected that the implementing class implement methods that call these - allows for customisation.
 * </p>
 *
 * @param <E> Class of the entity being held
 */
public abstract class EntityPool<E> implements DataCleanup {

    /**
     * @return A static list of all available entities.
     */
    protected abstract List<E> getPool();

    /**
     * @return A static list of all entities in use across the application.
     */
    protected abstract List<E> getPoolUsage();

    /**
     * @return A list of entities in use for this instance of the pool.
     */
    protected abstract List<E> getInstanceUsage();

    /**
     * Request a random item from the available entities.
     *
     * @return Requested entity
     */
    protected E request() {
        String type = "entity";

        if (getPool().size() > 0) {
            type = getPool().get(0).getClass().getSimpleName();
        }

        return request(getPool(), String.format("%s to become available", type));
    }

    /**
     * Request a random item from a subset of the available entities.
     *
     * @param subset      A filtered list
     * @param waitMessage Wait message if cannot get object immediately
     * @return Requested entity
     */
    protected E request(List<E> subset, String waitMessage) {

        ActionWait wait = new ActionWait()
                .withPollingIntervals(TimeUnit.SECONDS, 0, 15)
                .withTimeout(TimeUnit.MINUTES, 5)
                .withForMessage(waitMessage);

        int seed = (subset.size() > 1) ? ThreadLocalRandom.current().nextInt(0, subset.size() - 1) : 0;

        return wait.until(() -> {
            for (int i = seed; i < subset.size(); i++) {
                E entity = lock(subset.get(i));

                if (entity != null) {
                    return entity;
                }
            }

            for (int i = 0; i < seed; i++) {
                E entity = lock(subset.get(i));

                if (entity != null) {
                    return entity;
                }
            }

            return null;
        });
    }

    /**
     * Release specific entity back to the pool.
     *
     * @param entity Entity to release
     */
    protected void release(E entity) {
        getPoolUsage().remove(entity);
        getInstanceUsage().remove(entity);
    }

    /**
     * Locks entity preventing it from being used again until it has been released.
     *
     * @param entity Entity to lock
     * @return Passed entity if found in pool, otherwise null
     */
    protected E lock(E entity) {
        synchronized (getPoolUsage()) {
            if (!getPoolUsage().contains(entity)) {
                getPoolUsage().add(entity);
                getInstanceUsage().add(entity);

                return entity;
            }
        }

        return null;
    }

    /**
     * Release all locked entities.
     */
    public void releaseAll() {
        List<E> instanceUsage = getInstanceUsage();
        List<E> poolUsage = getPoolUsage();

        for (E used : instanceUsage) {
            poolUsage.remove(used);
        }

        instanceUsage.clear();
    }

    @Override
    public void cleanup() {
        releaseAll();
    }

}
