package org.concordion.cubano.driver.action;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

public class DurationParserTests {

    @Test
    public void nanos() {
        Duration d = Duration.ofNanos(1);

        assertThat(DurationParser.toLongString(d), is("0.000000001 Seconds"));
    }

    @Test
    public void millis() {
        Duration d = Duration.ofMillis(1);

        assertThat(DurationParser.toLongString(d), is("0.001 Seconds"));
    }

    @Test
    public void seconds() {
        assertThat(DurationParser.toLongString(Duration.of(12, ChronoUnit.SECONDS)), is("12 Seconds"));
        assertThat(DurationParser.toLongString(Duration.of(62, ChronoUnit.SECONDS)), is("1 Minute 2 Seconds"));
        assertThat(DurationParser.toLongString(Duration.of(5555, ChronoUnit.SECONDS)), is("1 Hour 32 Minutes 35 Seconds"));
    }

    @Test
    public void minutes() {
        Duration d = Duration.of(61, ChronoUnit.MINUTES);

        assertThat(DurationParser.toLongString(d), is("1 Hour 1 Minute"));
    }


    @Test
    public void hours() {
        Duration d = Duration.of(61, ChronoUnit.HOURS);

        assertThat(DurationParser.toLongString(d), is("61 Hours"));
    }

    @Test
    public void days() {
        assertThat(DurationParser.toLongString(Duration.ofDays(1)), is("24 Hours"));
        assertThat(DurationParser.toLongString(Duration.of(1, ChronoUnit.DAYS)), is("24 Hours"));
    }

    @Test
    public void weeks() {
        assertThat(DurationParser.toLongString(Duration.ofDays(7)), is("168 Hours"));
    }
}
