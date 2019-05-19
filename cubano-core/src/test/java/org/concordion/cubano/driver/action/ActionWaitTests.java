package org.concordion.cubano.driver.action;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ActionWaitTests {
    TestAppender appender;
    ch.qos.logback.classic.Logger logger;

    @Before
    public void beforeTest() {
        appender = new TestAppender();
        appender.start();

        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ActionWait.class);
        logger.addAppender(appender);
    }

    @After
    public void afterTest() {
        appender.stop();
        logger.detachAppender(appender);
    }

    @Test
    public void withTimeoutReduceInterval() {
        ActionWait wait = new ActionWait()
                .withTimeout(TimeUnit.SECONDS, 1)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 250);

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

            ILoggingEvent le = appender.getLoggingEvents().get(4);

            assertThat(le.getFormattedMessage(), startsWith("Pausing"));
            assertThat((int) le.getArgumentArray()[0], lessThan(250));

            assertThat(Duration.between(start, end).toMillis(), is(greaterThan(999L)));
        }
    }

    @Test
    public void withTimeoutStretchInterval() {
        ActionWait wait = new ActionWait()
                .withTimeout(TimeUnit.SECONDS, 1)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 220)
                .withTimeoutReturningResult();

        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();

        wait.until(() -> {
            return false;
        });

        Instant end = clock.instant();

        assertThat(wait.getAttempts(), is(4));

        ILoggingEvent le = appender.getLoggingEvents().get(4);

        assertThat(le.getFormattedMessage(), startsWith("Pausing"));
        assertThat((int) le.getArgumentArray()[0], greaterThan(220));

        assertThat(Duration.between(start, end).toMillis(), is(greaterThan(999L)));
    }

    @Test
    public void withPollingIntervalStartingWithZero() {
        ActionWait wait = new ActionWait()
                .withMaxAttempts(3)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 0, 5);

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
                .withPollingIntervals(TimeUnit.MILLISECONDS, 5);

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
        ActionWait wait = new ActionWait()
                .withMaxAttempts(4)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 10)
                .withWarningIntervals(TimeUnit.MILLISECONDS, 8, 12, 29, 30, 120)
                .withTimeoutReturningResult();

        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();

        boolean result = wait.until(() -> {
            return false;
        });

        Instant end = clock.instant();

        List<String> msgs = appender.getLoggingEvents().stream().filter(e -> e.getLevel().equals(Level.WARN)).map(e -> e.getFormattedMessage()).collect(Collectors.toList());

        assertThat(wait.getAttempts(), is(4));
        assertThat(msgs.size(), is(2));
        assertThat(msgs.get(0), is("Have been in waiting for over 12 for MILLISECONDS"));
        assertThat(msgs.get(1), is("Have been in waiting for over 30 for MILLISECONDS"));
        assertThat(Duration.between(start, end).toMillis(), is(greaterThan(9L)));
    }

    @Test
    public void withCustomWaitMessage() {
        ActionWait wait = new ActionWait()
                .withMaxAttempts(1)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 0)
                .withTimeoutReturningResult()
                .withMessage("some data to appear");

        boolean result = wait.until(() -> {
            return false;
        });

        String msg = appender.getLoggingEvents().get(0).getFormattedMessage();

        assertThat(msg, is("Trying for up to 1 attempts for some data to appear"));
    }

    @Test
    public void withDefaultWaitMessage() {
        ActionWait wait = new ActionWait()
                .withTimeout(TimeUnit.MILLISECONDS, 10)
                .withPollingIntervals(TimeUnit.MILLISECONDS, 0)
                .withTimeoutReturningResult();

        boolean result = wait.until(() -> {
            return false;
        });

        String msg = appender.getLoggingEvents().get(0).getFormattedMessage();

        assertThat(msg, is("Trying for up to 0.01 Seconds for action to complete successfully"));
    }
}

