package org.concordion.cubano.driver.action;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.support.ui.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Similar to Selenium's {@link org.openqa.selenium.support.ui.FluentWait
 * FluentWait} implementation but designed for long running tasks such as
 * querying a database until some data appears. Unlike
 * {@link org.openqa.selenium.support.ui.FluentWait FluentWait} it handles
 * exceptions other than RuntimeExceptions. Calling the until() method will
 * retry until either true or a non null value is returned.
 * <p>
 * 
 * Each ActionWait must defines the maximum amount of time to wait for a
 * condition (or alternatively the maximum number of attempts to make), as well
 * as the frequency with which to check the condition. Furthermore, the user may
 * configure the wait to ignore specific types of exceptions whilst waiting,
 * warning intervals to log a warning if action is taking longer than expected,
 * a custom message and the ability to override the default behaviour (throwing
 * a TimeOutException) and just return a value.
 * <p>
 * 
 * Sample usage:
 * 
 * <pre>
 * // Waiting 2 minutes for data to appear in database, checking for its presence
 * // immediately, then after 10 seconds, and every 5 seconds thereafter.
 * ActionWait wait = new ActionWait()
 *        .withTimeout(TimeUnit.MINUTES, 2)
 *        .withPollingIntervals(TimeUnit.SECONDS, 0, 10, 5)
 *        .withMessage("some data to appear");
 *
 * // Using Java 8 Lambda Expression
 * String value = wait.until(() -&gt; {
 *     ResultSet rs = stmt.executeQuery(query);
 *
 *     if (rs.next()) {
 *         return rs.getString("COLUMN");
 *     } else {
 *         return null;
 *     }
 * });
 *
 * // Using new Function - prior to Java 8
 * String value = wait.until(new IsTrue{@literal &lt;String&gt;}() {
 *     {@literal @Override}
 *     public String apply() {
 *         ...
 *     }
 * });
 * </pre>
 * 
 * <p>
 * <em>This class makes no thread safety guarantees.</em>
 *
 * @author Andrew Sumner
 */
public class ActionWait {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionWait.class);

    private Duration timeout = null;
    private int maxAttempts = 0;
    private TimeUnit pollingTimeUnit;
    private List<Integer> pollingIntervals = Lists.newArrayList();
    private TimeUnit warningTimeUnit = TimeUnit.SECONDS;
    private List<Integer> warningIntervals = Lists.newArrayList();
    private List<Class<? extends Throwable>> ignoredExceptions = Lists.newLinkedList();
    private String message = "";
    private boolean returnResult = false;

    private Clock clock;
    private Sleeper sleeper;
    private int attempts;
    private int warnings;

    public ActionWait() {
        clock = Clock.systemDefaultZone();
        sleeper = Sleeper.SYSTEM_SLEEPER;
    }

    /**
     * An interface the caller can implement to check that an action is complete.
     *
     * @param <V> The function's expected return type.
     */
    @FunctionalInterface
    public interface IsComplete<V> {
        /**
         * Method to implement.
         *
         * @return An object of the defined return type
         * @throws Exception If implemented method throws an exception
         */
        public V apply() throws Exception;
    }

    /**
     * Sets how long to wait for the evaluated condition to be true.
     * Either MaxAttempts or Timeout must be set, but they cannot be used together.
     *
     * @param unit The unit of time.
     * @param duration The timeout duration.
     * @return A self reference.
     */
    public ActionWait withTimeout(TimeUnit unit, long duration) {
        if (isWaitStyleMaxAttempts()) {
            throw new IllegalArgumentException("Timeout and MaxAttempts cannot be used together");
        }

        return withTimeout(Duration.of(duration, toChronoUnit(unit)));
    }

    /**
     * Sets how long to wait for the evaluated condition to be true.
     * Either MaxAttempts or Timeout must be set, but they cannot be used together.
     *
     * @param timeout The timeout duration.
     * @return A self reference.
     */
    public ActionWait withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets how many attempts to make while waiting for the for the evaluated condition to be true.
     * Either MaxAttempts or Timeout must be set, but they cannot be used together.
     *
     * @param attempts Maximum number of attempts to make.
     * @return A self reference.
     */
    public ActionWait withMaxAttempts(int attempts) {
        if (isWaitStyleTimeout()) {
            throw new IllegalArgumentException("Timeout and MaxAttempts cannot be used together");
        }

        this.maxAttempts = attempts;
        return this;
    }

    /**
     * Sets the polling intervals.  Required.
     *
     * @param unit      The unit of time.
     * @param intervals List of polling intervals.  To try immediately first value should be zero, if timeout has not expired and
     *                  then last interval is used repeatedly until reach timeout value or successful result returned.
     * @return A self reference.
     */
    public ActionWait withPollingIntervals(TimeUnit unit, Integer... intervals) {
        this.pollingTimeUnit = unit;
        this.pollingIntervals.addAll(Arrays.asList(intervals));
        return this;
    }

    /**
     * Sets a list of intervals at which to display a warning message if successful response has not been returned.
     *
     * @param unit      The unit of time.
     * @param intervals Each interval will be evaluated once the prior interval has passed.  An interval is measured from the
     *                  start of the action so ignores whatever intervals have been used before it.
     * @return A self reference.
     */
    public ActionWait withWarningIntervals(TimeUnit unit, Integer... intervals) {
        this.warningTimeUnit = unit;
        this.warningIntervals.addAll(Arrays.asList(intervals));
        return this;
    }

    /**
     * Adds a list of Exceptions that should be ignored if thrown when evaluating {@link #until(IsComplete)} method.
     *
     * @param exceptionType Exceptions to ignore
     * @return A self reference.
     */
    @SafeVarargs
    public final ActionWait ignoring(Class<? extends Throwable>... exceptionType) {
        this.ignoredExceptions.addAll(Arrays.asList(exceptionType));
        return this;
    }

    /**
     * Sets some text to be append to all messages.
     *
     * @param message description of action being performed.
     * @return A self reference.
     */
    public ActionWait withMessage(final String message) {
        this.message = message;
        return this;
    }

    /**
     * By default a timeout exception will be thrown, this will override.
     * <p>
     * that behaviour and cause a result to be returned
     *
     * @return A self reference.
     */
    public ActionWait withTimeoutReturningResult() {
        this.returnResult = true;
        return this;
    }

    /**
     * @return The number of attempts taken, starting at 1
     */
    public int getAttempts() {
        return attempts + 1;
    }

    /**
     * Repeatedly applies this instance's input value to the given function until one of the following
     * occurs:
     * <ol>
     * <li>the function returns either true or a not null value,</li>
     * <li>the function throws an unignored exception,</li>
     * <li>the timeout expires,
     * <li>the current thread is interrupted</li>
     * </ol>
     *
     * @param isTrue the parameter to pass to the {@link IsComplete}
     * @param <V>    The function's expected return type.
     * @return The functions' return value if the function returned something different
     * from null or false before the timeout expired.
     * @throws TimeoutException If the timeout expires.
     */
    public <V> V until(IsComplete<V> isTrue) {
        Throwable lastException = null;
        V value = null;

        attempts = 0;

        Instant start = clock.instant();
        Instant end = null;

        if (isWaitStyleTimeout()) {
            end = clock.instant().plus(timeout);
        }

        LOGGER.debug("Trying for up to {} for {}", getWaitStyle(), getMessage());

        while (hasMoreAttempts() || hasMoreTime(end)) {
            int interval = getNextPollingInterval(end);

            if (interval > 0) {
                try {
                    LOGGER.debug("Pausing for {} {} before check for {}", interval, pollingTimeUnit.toString().toLowerCase(), getMessage());
                    sleeper.sleep(Duration.of(interval, toChronoUnit(pollingTimeUnit)));
                } catch (InterruptedException e) {
                    throw new TimeoutException("Sleep failed", e);
                }
            }

            logWarningMessageIfRequired(start);

            try {
                value = isTrue.apply();

                if (value != null && Boolean.class.equals(value.getClass())) {
                    if (Boolean.TRUE.equals(value)) {
                        LOGGER.debug("{} found after {} attempts", getMessage(), getAttempts());
                        return value;
                    }
                } else if (value != null) {
                    LOGGER.debug("{} found after {} attempts", getMessage(), getAttempts());
                    return value;
                }
            } catch (Throwable e) {
                lastException = propagateIfNotIngored(e);
            }

            attempts++;
        }

        String timeoutMessage = String.format("Expected result was not found after %s while waiting for %s", getWaitStyle(), getMessage());

        if (returnResult) {
            LOGGER.debug(timeoutMessage);
            return value;
        } else {

            throw new TimeoutException(timeoutMessage, lastException);
        }
    }

    private String getWaitStyle() {
        if (isWaitStyleMaxAttempts()) {
            return maxAttempts + " attempts";
        } else if (isWaitStyleTimeout()) {
            return timeout.toString().toLowerCase();
        } else {
            throw new IllegalStateException("Either timeout or max attempts must be set");
        }
    }

    private boolean isWaitStyleMaxAttempts() {
        return maxAttempts > 0;
    }

    private boolean isWaitStyleTimeout() {
        return timeout != null;
    }

    private String getMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }

        return "action to complete successfully";
    }

    private Throwable propagateIfNotIngored(Throwable e) {
        for (Class<? extends Throwable> ignoredException : ignoredExceptions) {
            if (ignoredException.isInstance(e)) {
                return e;
            }
        }
        throw new RuntimeException(e);
    }

    private boolean hasMoreAttempts() {
        if (maxAttempts == 0) {
            return false;
        }

        return attempts < maxAttempts;
    }

    private boolean hasMoreTime(Instant end) {
        if (timeout == null) {
            return false;
        }

        return clock.instant().isBefore(end);
    }

    private int getNextPollingInterval(Instant end) {

        long interval;

        if (attempts > pollingIntervals.size() - 1) {
            interval = pollingIntervals.get(pollingIntervals.size() - 1);
        } else {
            interval = pollingIntervals.get(attempts);
        }

        long endTime = end.toEpochMilli();
        long currentTime = clock.instant().toEpochMilli();
        long waitTime = currentTime + TimeUnit.MILLISECONDS.convert(interval, pollingTimeUnit);
        long stretchTime = waitTime + TimeUnit.MILLISECONDS.convert(interval / 2, pollingTimeUnit);

        // If going above timeout limit bring it back to that limit
        // If closer than half current interval stretch it out
        if (waitTime > endTime || stretchTime > endTime) {
            interval = pollingTimeUnit.convert(endTime - currentTime, TimeUnit.MILLISECONDS) + 1;
        }

        return (int) interval;
    }

    private void logWarningMessageIfRequired(Instant start) {
        int interval;

        if (warnings > warningIntervals.size() - 1) {
            return;
        }

        interval = warningIntervals.get(warnings);

        Instant nextWarnTime = start.plus(interval, toChronoUnit(warningTimeUnit));

        if (clock.instant().equals(nextWarnTime) || clock.instant().isAfter(nextWarnTime)) {
            LOGGER.warn("Have been in waiting for over {} for {}", interval, warningTimeUnit.toString(), getMessage());

            warnings++;
        }
    }

    /**
     * Sleep for the requested time.
     *
     * @param timeUnit TimeUnit
     * @param duration Duration
     */
    public static void pause(TimeUnit timeUnit, int duration) {
        try {
            Thread.sleep(timeUnit.toMillis(duration));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        switch (timeUnit) {
        case NANOSECONDS:
            return ChronoUnit.NANOS;
        case MICROSECONDS:
            return ChronoUnit.MICROS;
        case MILLISECONDS:
            return ChronoUnit.MILLIS;
        case SECONDS:
            return ChronoUnit.SECONDS;
        case MINUTES:
            return ChronoUnit.MINUTES;
        case HOURS:
            return ChronoUnit.HOURS;
        case DAYS:
            return ChronoUnit.DAYS;
        default:
            throw new IllegalArgumentException("No ChronoUnit equivalent for " + timeUnit);
        }
    }
}
