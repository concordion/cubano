package org.concordion.cubano.driver.action;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class ActionWaitTests {


    @Test
    public void withTimeout() {
        ActionWait wait = new ActionWait()
                .withTimeout(TimeUnit.SECONDS, 2)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 500)
                .withMessage("some data to appear");

        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();

        try {
            wait.until(() -> {
                return (String) null;
            });

            fail("Test should never reach this point");

        } catch (TimeoutException ex) {
            Instant end = clock.instant();

            assertThat(wait.getAttempts(), is(4));
            assertThat(Duration.between(start, end).toMillis(), is(greaterThan(2000L)));
        }
    }

    @Test
    public void withPollingIntervalStartingWithZero() {
        ActionWait wait = new ActionWait()
                .withMaxAttempts(3)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 0, 5)
                .withMessage("some data to appear");

        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();

        try {
            wait.until(() -> {
                return (String) null;
            });

            fail("Test should never reach this point");

        } catch (TimeoutException ex) {
            Instant end = clock.instant();

            assertThat(wait.getAttempts(), is(3));
            assertThat(Duration.between(start, end).toMillis(), is(greaterThan(9L)));
        }
    }

    @Test
    public void withMaxAttempts() {
        ActionWait wait = new ActionWait()
                .withMaxAttempts(3)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 5)
                .withMessage("some data to appear");

        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();
        
        try {
            wait.until(() -> {
                return (String) null;
            });

            fail("Test should never reach this point");

        } catch (TimeoutException ex) {
            Instant end = clock.instant();

            assertThat(wait.getAttempts(), is(3));
            assertThat(Duration.between(start, end).toMillis(), is(greaterThan(14L)));
        }
    }

    @Test
    public void withIgnoredException() throws Exception {
        ActionWait wait = new ActionWait()
                .withMaxAttempts(3)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 5)
                .withMessage("some data to appear")
                .ignoring(ExecutionException.class);

        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();

        try {
            wait.until(() -> {
                throw new ExecutionException("Try again", null);
            });

            fail("Test should never reach this point");

        } catch (TimeoutException ex) {
            Instant end = clock.instant();

            assertThat(wait.getAttempts(), is(3));
            assertThat(Duration.between(start, end).toMillis(), is(greaterThan(14L)));
        }
    }

    @Test
    public void withTimeoutReturningValue() {
        ActionWait wait = new ActionWait()
                .withMaxAttempts(2)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 5)
                .withMessage("some data to appear")
                .withTimeoutReturningResult();

        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();

        boolean result = wait.until(() -> {
            return false;
        });

        Instant end = clock.instant();

        assertThat(wait.getAttempts(), is(2));
        assertThat(result, is(false));
        assertThat(Duration.between(start, end).toMillis(), is(greaterThan(9L)));
    }

    @Test
    public void withWarningIntervals() {
        TestAppender appender = new TestAppender();
        appender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ActionWait.class);
        logger.addAppender(appender);

        ActionWait wait = new ActionWait()
                .withMaxAttempts(4)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 10)
                .withWarningIntervals(TimeUnit.MILLISECONDS, 8, 12, 20, 30, 120)
                .withTimeoutReturningResult();

        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();

        boolean result = wait.until(() -> {
            return false;
        });

        Instant end = clock.instant();

        appender.stop();
        logger.detachAppender(appender);

        List<String> msgs = appender.getLoggingEvents().stream().filter(e -> e.getLevel().equals(Level.WARN)).map(e -> e.getFormattedMessage()).collect(Collectors.toList());

        assertThat(wait.getAttempts(), is(4));
        assertThat(msgs.size(), is(2));
        assertThat(msgs.get(0), is("Have been in waiting for over 12 for MILLISECONDS"));
        assertThat(msgs.get(1), is("Have been in waiting for over 30 for MILLISECONDS"));
        assertThat(Duration.between(start, end).toMillis(), is(greaterThan(9L)));
    }

    @Test
    public void withCustomWaitMessage() {
        TestAppender appender = new TestAppender();
        appender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ActionWait.class);
        logger.addAppender(appender);

        ActionWait wait = new ActionWait()
                .withMaxAttempts(1)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 0)
                .withTimeoutReturningResult()
                .withMessage("some data to appear");

        boolean result = wait.until(() -> {
            return false;
        });

        appender.stop();
        logger.detachAppender(appender);

        String msg = appender.getLoggingEvents().get(0).getFormattedMessage();

        assertThat(msg, is("Trying for up to 1 attempts for some data to appear"));
    }

    @Test
    public void withDefaultWaitMessage() {
        TestAppender appender = new TestAppender();
        appender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ActionWait.class);
        logger.addAppender(appender);

        ActionWait wait = new ActionWait()
                .withTimeout(TimeUnit.MILLISECONDS, 10)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 0)
                .withTimeoutReturningResult();

        boolean result = wait.until(() -> {
            return false;
        });

        appender.stop();
        logger.detachAppender(appender);

        String msg = appender.getLoggingEvents().get(0).getFormattedMessage();

        assertThat(msg, is("Trying for up to 0.01 Seconds for action to complete successfully"));
    }
}

