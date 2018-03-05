package org.concordion.cubano.driver.web.provider;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public enum CheckForUpdates {
    ALWAYS, HOURLY, DAILY, WEEKLY, MONTHLY, NEVER;

    private ChronoUnit unit;

    static {
        HOURLY.unit = ChronoUnit.HOURS;
        DAILY.unit = ChronoUnit.DAYS;
        DAILY.unit = ChronoUnit.DAYS;
        WEEKLY.unit = ChronoUnit.WEEKS;
        MONTHLY.unit = ChronoUnit.MONTHS;
    }

    public boolean recheckIsRequired(Date fromDate) {
        switch (this) {
        case ALWAYS:
            return true;
        case NEVER:
            return false;
        default:
        }
        
        Instant now = Instant.now();
        Instant nextCheck = Instant.ofEpochMilli(fromDate.getTime()).plus(1, unit);

        return nextCheck.isBefore(now);
    }
}
