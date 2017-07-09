package org.concordion.cubano.utils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs the duration of an action.
 *
 * @author Andrew Sumner
 */
public class ActionTimer {
    private final ZonedDateTime startwait;
    private final Logger logger;

    private ActionTimer(Logger logger) {
        this.logger = logger;
        this.startwait = now();
    }

    /**
     * Start a new timer using, no logging will be performed.
     *
     * @return new ActionTimer
     */
    public static ActionTimer start() {
        return new ActionTimer(LoggerFactory.getLogger(ActionTimer.class.getName()));
    }

    /**
     * Start a new timer, providing a logger to use.
     *
     * @param logger Logger to use for any logging
     * @return new ActionTimer
     */
    public static ActionTimer start(Logger logger) {
        return new ActionTimer(logger);
    }

    /**
     * Start a new timer, logging the supplied message.
     * <p>
     * Example:
     * <p>
     * <pre>ActionTimer timer = ActionTimer.start(LOGGER, "Starting action");</pre>
     *
     * @param logger Logger to use for any logging
     * @param format Formatted message string, argument place holders can be embedded with {} marker
     * @param args   List of arguments for the message format string
     * @return new ActionTimer
     */
    public static ActionTimer start(Logger logger, String format, Object... args) {
        logger.debug(format, args);

        return new ActionTimer(logger);
    }

    /**
     * The duration between the time the start method was called and now.
     *
     * @return Duration
     */
    public Duration duration() {
        return Duration.between(startwait, now());
    }

    /**
     * Log supplied message.
     * <p>
     * Example:
     * <p>
     * <pre>timer.stop("Action completed in {} seconds", timer.duration().getSeconds());</pre>
     *
     * @param format Formatted message string, argument place holders can be embedded with {} marker
     * @param args   List of arguments for the message format string
     */
    public void stop(String format, Object... args) {
        stop(LogLevel.DEBUG, format, args);
    }

    /**
     * Log the supplied message at the specified level.
     *
     * @param level  Log level
     * @param format Formatted message string, argument place holders can be embedded with {} marker
     * @param args   List of arguments for the message format string
     */
    public void stop(LogLevel level, String format, Object... args) {
        switch (level) {
            case INFO:
                logger.info(format, args);
                break;
            case DEBUG:
                logger.debug(format, args);
                break;
            case TRACE:
                logger.trace(format, args);
                break;
            default:
                throw new IllegalArgumentException("Unknown LogLevel");
        }
    }

    private static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    /**
     * Allowed log levels.
     */
    public enum LogLevel {
        INFO, DEBUG, TRACE;
    }

    /**
     * Check if timer has passed supplied duration.
     *
     * @param unit     Unit of time
     * @param duration Duration
     * @return True if time since timer was started is more that supplied value
     */
    public boolean hasPassed(TimeUnit unit, long duration) {
        return startwait.plusSeconds(unit.toSeconds(duration)).isAfter(now());
    }
}
