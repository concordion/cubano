package org.concordion.cubano.driver.action;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ActionWaitTests {


    @Test
    public void withTimeout() throws Exception {
        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();

        try {
            ActionWait wait = new ActionWait()
                    .withTimeout(TimeUnit.SECONDS, 2)
                    .withPollingIntervals(TimeUnit.MILLISECONDS, 0, 100, 500)
                    .withMessage("some data to appear");

            wait.until(() -> {
                return (String) null;
            });

            throw new IllegalStateException("Should have timed out");

        } catch (TimeoutException ex) {
            Instant end = clock.instant();

            assertThat(Duration.between(start, end).toMillis(), is(greaterThan(2000L)));
            assertThat(Duration.between(start, end).toMillis(), is(lessThan(2500L)));
        }
    }

    @Test
    public void withMaxAttempts() throws Exception {
        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();
        
        try {
            ActionWait wait = new ActionWait()
                    .withMaxAttempts(3)
                    .withPollingIntervals(TimeUnit.MILLISECONDS, 5)
                    .withMessage("some data to appear");

            wait.until(() -> {
                return (String) null;
            });

            throw new Exception("Should have timed out");

        } catch (TimeoutException ex) {
            Instant end = clock.instant();

            assertThat(Duration.between(start, end).toMillis(), is(greaterThan(50L)));
            assertThat(Duration.between(start, end).toMillis(), is(lessThan(400L)));
        }
    }
}
