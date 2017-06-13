package org.concordion.cubano.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.support.ui.Clock;
import org.openqa.selenium.support.ui.Duration;
import org.openqa.selenium.support.ui.Sleeper;
import org.openqa.selenium.support.ui.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Similar to Selenium's {@link org.openqa.selenium.support.ui.FluentWait FluentWait} implementation but designed for long running tasks such as querying a
 * database until some data appears.  Unlike {@link org.openqa.selenium.support.ui.FluentWait FluentWait} it handles exceptions other than RuntimeExceptions.
 * Calling the until() method will retry until either true or a non null value is returned.
 * <p>
 * <p>
 * Each ActionWait must defines the maximum amount of time to wait for a condition, as well as
 * the frequency with which to check the condition. Furthermore, the user may configure the wait to
 * ignore specific types of exceptions whilst waiting, warning intervals to log a warning if action is taking
 * longer than expected, a custom message and the ability to override the default behaviour (throwing a TimeOutException)
 * and just return a value.
 * </p>
 * <p>
 * <p>
 * Sample usage: <pre>
 * // Waiting 2 minutes for data to appear in database, checking for its presence
 * // immediately, then after 10 seconds, and every 5 seconds thereafter.
 * ActionWait wait = new ActionWait()
 *        .withTimeout(TimeUnit.MINUTES, 2)
 *        .withPollingIntervals(TimeUnit.SECONDS, 0, 10, 5)
 *        .withForMessage("some data to appear");
 * <p>
 * // Using Java 8 Lambda Expression
 * String value = wait.until(() -> {
 *     ResultSet rs = stmt.executeQuery(query);
 * <p>
 *     if (rs.next()) {
 *         return rs.getString("COLUMN");
 *     } else {
 *         return null;
 *     }
 * });
 * <p>
 * // Using new Function - prior to Java 8
 * String value = wait.until(new IsTrue{@literal <String>}() {
 *     {@literal @Override}
 *     public String apply() {
 *         ...
 *     }
 * });
 * </pre>
 * </p>
 * <p>
 * <em>This class makes no thread safety guarantees.</em>
 *
 * @author Andrew Sumner
 */
public class ActionWait {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionWait.class);

    private Duration timeout;
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
     * Sets how long to wait for the evaluated condition to be true. Required.
     *
     * @param unit     The unit of time.
     * @param duration The timeout duration.
     * @return A self reference.
     */
    public ActionWait withTimeout(TimeUnit unit, long duration) {
        this.timeout = new Duration(duration, unit);
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
     * Sets some text to be append to timeout and warning messages.
     *
     * @param message to be appended to default.
     * @return A self reference.
     */
    public ActionWait withForMessage(final String message) {
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
        clock = new SystemClock();
        sleeper = Sleeper.SYSTEM_SLEEPER;

        boolean loggedWait = false;
        long start = clock.now();
        long end = clock.laterBy(timeout.in(TimeUnit.MILLISECONDS));

        while (hasMoreTime(clock, end)) {
            int interval = getNextPollingInterval(clock, end);

            if (interval > 0) {
                if (!loggedWait) {
                    loggedWait = true;
                    LOGGER.debug("Waiting for up to {}{}{}", timeout.toString().toLowerCase(), (hasMessage() ? " for " : ""), message);
                }

                try {
                    LOGGER.trace("Pausing for {} {}", interval, pollingTimeUnit.toString().toLowerCase());
                    sleeper.sleep(new Duration(interval, pollingTimeUnit));
                } catch (InterruptedException e) {
                    throw new TimeoutException("Sleep failed", e);
                }
            }

            logWarningMessageIfRequired(start);

            try {
                value = isTrue.apply();
                if (value != null && Boolean.class.equals(value.getClass())) {
                    if (Boolean.TRUE.equals(value)) {
                        return value;
                    }
                } else if (value != null) {
                    return value;
                }
            } catch (Throwable e) {
                lastException = propagateIfNotIngored(e);
            }

            attempts++;
        }

        if (returnResult) {
            return value;
        } else {
            String toAppend = hasMessage() ? " waiting for " + message : "";
            String timeoutMessage = String.format("Timed out after %s%s", timeout.toString().toLowerCase(), toAppend);

            throw new TimeoutException(timeoutMessage, lastException);
        }
    }

    private boolean hasMessage() {
        return message != null && !message.isEmpty();
    }

    private Throwable propagateIfNotIngored(Throwable e) {
        for (Class<? extends Throwable> ignoredException : ignoredExceptions) {
            if (ignoredException.isInstance(e)) {
                return e;
            }
        }
        throw Throwables.propagate(e);
    }

    private boolean hasMoreTime(Clock clock, long end) {
        return clock.isNowBefore(end);
    }

    private int getNextPollingInterval(Clock clock, long end) {
        long interval;

        if (attempts > pollingIntervals.size() - 1) {
            interval = pollingIntervals.get(pollingIntervals.size() - 1);
        } else {
            interval = pollingIntervals.get(attempts);
        }

        long currentTime = clock.now();
        long waitTime = currentTime + TimeUnit.MILLISECONDS.convert(interval, pollingTimeUnit);
        long stretchTime = waitTime + TimeUnit.MILLISECONDS.convert(interval / 2, pollingTimeUnit);

        // If going above timeout limit bring it back to that limit
        // If closer than half current interval stretch it out
        if (waitTime > end || stretchTime > end) {
            interval = pollingTimeUnit.convert(end - currentTime, TimeUnit.MILLISECONDS) + 1;
        }

        return (int) interval;
    }

    private void logWarningMessageIfRequired(long startTimeInMillis) {
        int interval;

        if (warnings > warningIntervals.size() - 1) {
            return;
        }

        interval = warningIntervals.get(warnings);

        long nextWarnTime = startTimeInMillis + TimeUnit.MILLISECONDS.convert(interval, warningTimeUnit);

        if (clock.now() >= nextWarnTime) {
            String toAppend = hasMessage() ? " waiting for " + message : "";
            LOGGER.warn("Have been in waiting for over {} {}{}", interval, warningTimeUnit.toString(), toAppend);

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
}
