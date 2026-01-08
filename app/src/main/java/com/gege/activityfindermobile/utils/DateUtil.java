package com.gege.activityfindermobile.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Utility class for date parsing and formatting operations */
public class DateUtil {

    // ISO 8601 format used by backend
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private DateUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Parse ISO 8601 date string to Date object
     *
     * @param dateString Date string in format "yyyy-MM-dd'T'HH:mm:ss"
     * @return Parsed Date object or null if parsing fails
     */
    public static Date parseIsoDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_FORMAT, Locale.US);
            return sdf.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Format Date object to ISO 8601 string
     *
     * @param date Date object to format
     * @return Formatted date string or null if date is null
     */
    public static String formatIsoDate(Date date) {
        if (date == null) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_FORMAT, Locale.US);
        return sdf.format(date);
    }

    /**
     * Check if a date is in the past
     *
     * @param dateString Date string in ISO format
     * @return true if date is in the past, false otherwise
     */
    public static boolean isPast(String dateString) {
        Date date = parseIsoDate(dateString);
        if (date == null) {
            return false;
        }
        return date.before(new Date());
    }

    /**
     * Check if a date is in the future
     *
     * @param dateString Date string in ISO format
     * @return true if date is in the future, false otherwise
     */
    public static boolean isFuture(String dateString) {
        Date date = parseIsoDate(dateString);
        if (date == null) {
            return false;
        }
        return date.after(new Date());
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "in 3 days")
     *
     * @param dateString Date string in ISO format
     * @return Relative time string
     */
    public static String getRelativeTimeString(String dateString) {
        Date date = parseIsoDate(dateString);
        if (date == null) {
            return "";
        }

        long diff = new Date().getTime() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            long weeks = days / 7;
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        }
    }

    /**
     * Check if a display format date (MMM dd, yyyy) is in the past This compares only the date
     * portion, ignoring time
     *
     * @param dateString Date string in "MMM dd, yyyy" format (e.g., "Nov 15, 2025")
     * @return true if date is before today, false otherwise
     */
    public static boolean isDisplayDateExpired(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date activityDate = displayFormat.parse(dateString);

            if (activityDate == null) {
                return false;
            }

            // Reset time to start of day for both dates to compare just the date portion
            java.util.Calendar calActivity = java.util.Calendar.getInstance();
            calActivity.setTime(activityDate);
            calActivity.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calActivity.set(java.util.Calendar.MINUTE, 0);
            calActivity.set(java.util.Calendar.SECOND, 0);
            calActivity.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar calToday = java.util.Calendar.getInstance();
            calToday.setTime(new Date());
            calToday.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calToday.set(java.util.Calendar.MINUTE, 0);
            calToday.set(java.util.Calendar.SECOND, 0);
            calToday.set(java.util.Calendar.MILLISECOND, 0);

            // Activity is expired if the date is before today (not including today)
            return calActivity.before(calToday);
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Format ISO date to display date format (MMM dd, yyyy)
     *
     * @param isoDateString ISO date string (yyyy-MM-dd'T'HH:mm:ss)
     * @return Formatted date string or null if parsing fails
     */
    public static String formatToDisplayDate(String isoDateString) {
        Date date = parseIsoDate(isoDateString);
        if (date == null) {
            return null;
        }

        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return displayFormat.format(date);
    }

    /**
     * Format ISO date to display time format (hh:mm a)
     *
     * @param isoDateString ISO date string (yyyy-MM-dd'T'HH:mm:ss)
     * @return Formatted time string or null if parsing fails
     */
    public static String formatToDisplayTime(String isoDateString) {
        Date date = parseIsoDate(isoDateString);
        if (date == null) {
            return null;
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return timeFormat.format(date);
    }

    /**
     * Combine display date and time into ISO format
     *
     * @param displayDate Date string in "MMM dd, yyyy" format
     * @param displayTime Time string in "hh:mm a" format
     * @return ISO formatted date-time string or null if parsing fails
     */
    public static String combineToIsoFormat(String displayDate, String displayTime) {
        if (displayDate == null
                || displayDate.isEmpty()
                || displayTime == null
                || displayTime.isEmpty()) {
            return null;
        }

        try {
            // Parse date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = dateFormat.parse(displayDate);

            // Parse time
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date time = timeFormat.parse(displayTime);

            if (date == null || time == null) {
                return null;
            }

            // Combine date and time using Calendar
            java.util.Calendar dateCal = java.util.Calendar.getInstance();
            dateCal.setTime(date);

            java.util.Calendar timeCal = java.util.Calendar.getInstance();
            timeCal.setTime(time);

            dateCal.set(
                    java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
            dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
            dateCal.set(java.util.Calendar.SECOND, 0);
            dateCal.set(java.util.Calendar.MILLISECOND, 0);

            // Format to ISO
            return formatIsoDate(dateCal.getTime());
        } catch (ParseException e) {
            return null;
        }
    }
}
