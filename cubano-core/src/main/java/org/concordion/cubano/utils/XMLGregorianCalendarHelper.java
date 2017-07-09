package org.concordion.cubano.utils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Useful methods to play with XMLGregorianCalendar.
 * <p>
 * <p>
 * Note: requires minimum of Java 1.8
 * </p>
 *
 * @author Priya Narayanan
 */
public class XMLGregorianCalendarHelper {

    private XMLGregorianCalendarHelper() {
    }

    /**
     * Converts current date/time in XMLGregorianCalendar format.
     *
     * @return XMLGregorianCalendar
     * @throws DatatypeConfigurationException, ParseException
     */

    public static XMLGregorianCalendar getTodaysDateInXMLGregorian() throws DatatypeConfigurationException, ParseException {
        return ISODateTimeFormat.toXMLGregorianCalendar(LocalDateTime.now());
    }

    /**
     * @param startDate XMLGregorianCalendar
     * @param numOfDays Integer
     * @return XMLGregorianCalendar
     * @throws DatatypeConfigurationException
     */

    public static XMLGregorianCalendar addDays(XMLGregorianCalendar startDate, int numOfDays) throws DatatypeConfigurationException {

        GregorianCalendar calendar = startDate.toGregorianCalendar();
        calendar.add(Calendar.DATE, numOfDays);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    }

}
