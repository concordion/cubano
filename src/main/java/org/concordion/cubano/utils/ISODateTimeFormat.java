package org.concordion.cubano.utils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Convert from/to ISO8601 formated dates such as '2016-04-28T00:02:40Z'.
 * <p>
 * <p>Note: requires minimum of Java 1.8</p>
 *
 * @author Andrew Sumner
 */
public class ISODateTimeFormat {
    private ISODateTimeFormat() {
    }

    public static final String SHORT_DATE = "yyyy-MM-dd";
    public static final String LONG_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DISPLAY_SHORT_DATE = "dd/MM/yyyy";
    public static final String DISPLAY_LONG_DATE = "dd/MM/yyyy HH:mm:ss";
    public static final String DISPLAY_FULL_LONG_DATE = "EEE, d MMM yyyy HH:mm:ss";
    public static final String WORD_DATE = "dd MMM yyyy";

    /**
     * Parses an ISO8601 formatted date string.
     *
     * @param iso8601Date String containing ISO8601 formatted date
     * @return LocalDateTime
     * @throws ParseException
     */
    public static LocalDateTime parse(String iso8601Date) throws ParseException {
        if (iso8601Date.trim().matches(".*[a-zA-Z]")) {
            ZonedDateTime zdt = ZonedDateTime.parse(iso8601Date);
            zdt = zdt.withZoneSameInstant(ZoneId.systemDefault());
            return zdt.toLocalDateTime();
        }

        return LocalDateTime.parse(iso8601Date);
    }

    /**
     * Parses a date string using the supplied formatter.
     *
     * @param date    Date string
     * @param pattern Pattern to use
     * @return LocalDateTime
     * @throws ParseException
     */
    public static LocalDateTime parse(String date, String pattern) throws ParseException {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats a LocalDateTime into an ISO8601 date/time string for the supplied ZoneId.
     *
     * @param date   LocalDateTime
     * @param zoneId ZoneId
     * @return ISO8601 date time string
     * @throws ParseException
     */
    public static String format(LocalDateTime date, ZoneId zoneId) throws ParseException {
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(zoneId);

        return zdt.toString();
    }

    /**
     * Formats a LocalDateTime into an ISO8601 date/time string.
     *
     * @param date LocalDateTime
     * @return ISO8601 date time string
     * @throws ParseException
     */
    public static String formatAsLocalDateTimeString(LocalDateTime date) throws ParseException {
        return date.toString();
    }

    /**
     * Formats a LocalDateTime into an ISO8601 date/time string - always including seconds even if zero.
     *
     * @param date LocalDateTime
     * @return ISO8601 date time string
     * @throws ParseException
     */
    public static String formatAsLocalDateTime(LocalDateTime date) throws ParseException {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Formats a LocalDateTime into an ISO8601 UTC date/time string.
     *
     * @param date LocalDateTime
     * @return ISO8601 date time string
     * @throws ParseException
     */
    public static String formatAsUTCDateTimeString(LocalDateTime date) throws ParseException {

        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneId.of("Z"));

        return zdt.toString();
    }

    /**
     * Formats a LocalDateTime into an ISO8601 UTC date/time string.
     *
     * @param date LocalDateTime
     * @return ISO8601 date time string
     * @throws ParseException
     */
    public static String formatAsUTCDateTime(LocalDateTime date) throws ParseException {
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneId.of("Z"));

        return zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * Formats a LocalDateTime into an ISO8601 UTC date/time string customised for BPM as it cannot handle variable length milliseconds.
     *
     * @param date LocalDateTime
     * @return ISO8601 date time string
     * @throws ParseException
     */
    public static String formatAsUTCDateTimeBPM(LocalDateTime date) throws ParseException {
        return formatAsUTCDateTimeBPM(date, "uuuu-MM-dd'T'HH:mm:ss.SSSX");
    }

    /**
     * Formats a LocalDateTime into an ISO8601 UTC date/time.
     *
     * @param date   LocalDateTime
     * @param format date pattern
     * @return ISO8601 date time string
     * @throws ParseException
     */
    public static String formatAsUTCDateTimeBPM(LocalDateTime date, String format) throws ParseException {
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneId.of("Z"));

        return zdt.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Formats a date using the specified pattern.
     *
     * @param date    Date to format
     * @param pattern Pattern to use
     * @return Formatted date string
     * @throws ParseException
     */
    public static String format(LocalDateTime date, String pattern) throws ParseException {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Create a new XMLGregorianCalendar by parsing the String as a lexical representation, see {@link DatatypeFactory#newXMLGregorianCalendar(String)}.
     *
     * @param lexicalRepresentation Lexical representation of one the eight XML Schema date/time datatypes.
     * @return XMLGregorianCalendar created from the lexicalRepresentation.
     * @throws ParseException
     */
    public static XMLGregorianCalendar toXMLGregorianCalendar(String lexicalRepresentation) throws ParseException {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(lexicalRepresentation);
        } catch (DatatypeConfigurationException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    /**
     * Create a new XMLGregorianCalendar from the LocalDateTime.
     *
     * @param ldt LocalDateTime
     * @return XMLGregorianCalendar created from the LocalDateTime.
     * @throws ParseException
     */
    public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDateTime ldt) throws ParseException {
        return toXMLGregorianCalendar(formatAsUTCDateTimeBPM(ldt));
    }

    /**
     * Return a LocalDateTime representation of the XMLGregorianCalendar.
     *
     * @param xcal XMLGregorianCalendar
     * @return A LocalDateTime representation of the XMLGregorianCalendar
     * @throws ParseException
     */
    public static LocalDateTime fromXMLGregorianCalendarToLocalDT(XMLGregorianCalendar xcal) throws ParseException {
        return parse(xcal.toGregorianCalendar().getTime().toInstant().toString());
    }

    /**
     * Return the lexical representation of the XMLGregorianCalendar, see {@link XMLGregorianCalendar#toXMLFormat()}.
     *
     * @param xcal XMLGregorianCalendar
     * @return XML, as String, representation of the XMLGregorianCalendar
     */
    public static String fromXMLGregorianCalendarToString(XMLGregorianCalendar xcal) {
        return xcal.toXMLFormat();
    }
}
