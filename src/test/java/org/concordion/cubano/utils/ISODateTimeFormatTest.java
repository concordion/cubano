package org.concordion.cubano.utils;

import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.*;

public class ISODateTimeFormatTest {
    @Test
    public void custom() throws ParseException {
        LocalDateTime date;

        // Custom
        date = ISODateTimeFormat.parse("2016-07-01T02:09:18.76Z");
        assertEquals("ISO8601 Local Date", "2016-07-01T02:09:18.760Z", ISODateTimeFormat.formatAsUTCDateTimeBPM(date));

        date = ISODateTimeFormat.parse("2016-07-01T02:09:18.7Z");
        assertEquals("ISO8601 Local Date", "2016-07-01T02:09:18.700Z", ISODateTimeFormat.formatAsUTCDateTimeBPM(date));

        date = ISODateTimeFormat.parse("2016-07-01T02:09:00.76Z");
        assertEquals("ISO8601 Local Date", "2016-07-01T02:09:00.760Z", ISODateTimeFormat.formatAsUTCDateTimeBPM(date));

        date = ISODateTimeFormat.parse("2016-07-01T02:09:00Z");
        assertEquals("ISO8601 Local Date", "2016-07-01T02:09:00.000Z", ISODateTimeFormat.formatAsUTCDateTimeBPM(date));
    }

    @Test
    public void zeroMinutesSeconds() throws ParseException {
        LocalDateTime date;

        // Zero Minutes/Seconds
        date = ISODateTimeFormat.parse("2016-04-26T15:00:00");
        assertEquals("ISO8601 Local Date", "2016-04-26T15:00", ISODateTimeFormat.formatAsLocalDateTimeString(date));
        assertEquals("ISO8601 UTC TimeZone", "2016-04-26T03:00Z", ISODateTimeFormat.formatAsUTCDateTimeString(date));
        assertEquals("ISO8601 Local Date", "2016-04-26T15:00:00", ISODateTimeFormat.formatAsLocalDateTime(date));
        assertEquals("ISO8601 UTC TimeZone", "2016-04-26T03:00:00Z", ISODateTimeFormat.formatAsUTCDateTime(date));

        // Non Zero Minutes/Seconds
        date = ISODateTimeFormat.parse("2016-04-26T15:16:55");
        assertEquals("Local Date", "2016-04-26T15:16:55", date.toString());
        assertEquals("ISO8601 Local Date", "2016-04-26T15:16:55", ISODateTimeFormat.formatAsLocalDateTimeString(date));
        assertEquals("ISO8601 UTC TimeZone", "2016-04-26T03:16:55Z", ISODateTimeFormat.formatAsUTCDateTimeString(date));
        assertEquals("ISO8601 Default TimeZone", "2016-04-26T15:16:55+12:00[" + ZoneId.systemDefault().getId() + "]", ISODateTimeFormat.format(date, ZoneId.systemDefault()));
    }

    @Test
    public void daylightSavings() throws ParseException {
        LocalDateTime date;

        // Daylight Savings
        date = ISODateTimeFormat.parse("2016-03-26T15:16:55");
        assertEquals("ISO8601 Local Date Daylight Savings", "2016-03-26T15:16:55", ISODateTimeFormat.formatAsLocalDateTimeString(date));
        assertEquals("ISO8601 Local Date Daylight Savings", "2016-03-26T02:16:55Z", ISODateTimeFormat.formatAsUTCDateTimeString(date));
        assertEquals("ISO8601 Default TimeZone Daylight Savings",
                "2016-03-26T15:16:55+13:00[" + ZoneId.systemDefault().getId() + "]",
                ISODateTimeFormat.format(date, ZoneId.systemDefault()));

        date = ISODateTimeFormat.parse("2016-04-26T03:16:55Z");
        assertEquals("UTC Date", "2016-04-26T15:16:55", date.toString());
        assertEquals("ISO8601 UTC Date", "2016-04-26T03:16:55Z", ISODateTimeFormat.formatAsUTCDateTimeString(date));
    }

    @Test
    public void nanosecods() throws ParseException {
        LocalDateTime date;

        // Nanoseconds
        date = ISODateTimeFormat.parse("2016-04-26T03:16:55.189Z");
        assertEquals("ISO8601 Local NANO", "2016-04-26T15:16:55.189", ISODateTimeFormat.formatAsLocalDateTimeString(date));

        date = ISODateTimeFormat.parse("2016-04-26T03:16:55.1Z");
        assertEquals("ISO8601 Local NANO", "2016-04-26T15:16:55.100", ISODateTimeFormat.formatAsLocalDateTimeString(date));

        date = ISODateTimeFormat.parse("2016-04-26T03:16:55.100200300Z");
        assertEquals("ISO8601 Local NANO", "2016-04-26T15:16:55.100200300", ISODateTimeFormat.formatAsLocalDateTimeString(date));
    }

    @Test
    public void customFormat() throws ParseException {
        LocalDateTime date;

        // Custom Format
        date = ISODateTimeFormat.parse("2016-04-26T03:16:55.923Z");
        assertEquals("Custom Format", "2016-04-26", ISODateTimeFormat.format(date, ISODateTimeFormat.SHORT_DATE));
        assertEquals("Custom Format", "26/04/2016 15:16:55", ISODateTimeFormat.format(date, ISODateTimeFormat.DISPLAY_LONG_DATE));
    }

    @Test
    public void gregorianCalendar() throws ParseException {
        LocalDateTime date;

        // XMLGregorianCalendar
        XMLGregorianCalendar xcal = ISODateTimeFormat.toXMLGregorianCalendar("2016-04-26T03:16:55.923Z");

        assertEquals("XMLGregorianCalendar", "Tue Apr 26 15:16:55 NZST 2016", xcal.toGregorianCalendar().getTime().toString());
        assertEquals("XMLGregorianCalendar", "2016-04-26T03:16:55.923Z", xcal.toGregorianCalendar().getTime().toInstant().toString());

        assertEquals("XMLGregorianCalendar", "2016-04-26T03:16:55.923Z", ISODateTimeFormat.fromXMLGregorianCalendarToString(xcal));
        date = ISODateTimeFormat.fromXMLGregorianCalendarToLocalDT(xcal);

        assertEquals("XMLGregorianCalendar", "2016-04-26T15:16:55.923", ISODateTimeFormat.formatAsLocalDateTime(date));
    }

}