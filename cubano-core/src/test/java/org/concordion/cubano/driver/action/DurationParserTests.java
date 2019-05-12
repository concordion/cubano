package org.concordion.cubano.driver.action;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

public class DurationParserTests {

    @Test
    public void seconds() {

        Duration d = Duration.of(12, ChronoUnit.SECONDS);

        assertThat(DurationParser.toLongString(d), is("12 Seconds"));
    }

    @Test
    public void millis() {

        Duration d = Duration.ofMillis(1);

        assertThat(DurationParser.toLongString(d), is("0.001 Seconds"));
    }

    @Test
    public void nanos() {

        Duration d = Duration.ofNanos(1);

        assertThat(DurationParser.toLongString(d), is("0.000000001 Seconds"));
    }

    @Test
    public void days() {

        Duration d = Duration.ofDays(1);

        assertThat(DurationParser.toLongString(d), is("24 Hours"));
    }
}
