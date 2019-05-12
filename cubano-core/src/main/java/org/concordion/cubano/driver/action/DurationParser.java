package org.concordion.cubano.driver.action;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {

    /**
     * A string representation of this duration using ISO-8601 seconds based representation, such as PT8H6M12.345S.
     * 
     * The format of the returned string will be PTnHnMnS, where n is the relevant hours, minutes or seconds part of the duration. Any fractional seconds are placed after a decimal
     * point i the seconds section.If a section has a zero value, it is omitted.The hours, minutes and seconds will all have the same sign.
     * 
     * Examples:
     * "20.345 seconds" -- "PT20.345S
     * "15 minutes" (15 * 60 seconds) -- "PT15M"
     * "10 hours" (10 * 3600 seconds) -- "PT10H"
     * "2 days" (2 * 86400 seconds) -- "PT48H"
     * 
     * https://en.wikipedia.org/wiki/ISO_8601
     * P is the duration designator (for period) placed at the start of the duration representation.
     * Y is the year designator that follows the value for the number of years.
     * M is the month designator that follows the value for the number of months.
     * W is the week designator that follows the value for the number of weeks.
     * D is the day designator that follows the value for the number of days.
     * T is the time designator that precedes the time components of the representation.
     * H is the hour designator that follows the value for the number of hours.
     * M is the minute designator that follows the value for the number of minutes.
     * S is the second designator that follows the value for the number of seconds.
     *
     * @param duration
     * @return an ISO-8601 representation of this duration, not null
     */
    public static String toLongString(Duration duration) {
        if (duration == null) {
            return null;
        }

        String d = duration.toString().toUpperCase();
        
        if (!d.substring(0,  1).equalsIgnoreCase("P")) {
            return d;
        }
        
        StringBuilder s = new StringBuilder();
        Pattern pattern = Pattern.compile("[\\d\\.]*\\D");
        Matcher matcher = pattern.matcher(d);
        boolean timeComponent = false;
        
        while (matcher.find()) {
            String group = matcher.group();
            String element = group.substring(group.length() - 1);

            if (element.equalsIgnoreCase("P")) {
                timeComponent = false;
                continue;
            }

            if (element.equalsIgnoreCase("T")) {
                timeComponent = true;
                continue;
            }

            String interval = group.substring(0, group.length() - 1);

            s.append(s.length() > 0 ? " " : "").append(interval).append(" ");

            if (!timeComponent) {
                switch (element) {
                case "Y":
                    s.append("Year");
                    break;
                case "M":
                    s.append("Month");
                    break;
                case "W":
                    s.append("Week");
                    break;
                case "D":
                    s.append("Day");
                    break;
                default:
                    s.append(element);
                    break;
                }
            }

            if (timeComponent) {
                switch (element) {
                case "H":
                    s.append("Hour");
                    break;
                case "M":
                    s.append("Minute");
                    break;
                case "S":
                    s.append("Second");
                    break;
                default:
                    s.append(element);
                    break;
                }
            }

            if (Double.parseDouble(interval) != 1) {
                s.append("s");
            }
        }


        return s.toString();
    }

}
