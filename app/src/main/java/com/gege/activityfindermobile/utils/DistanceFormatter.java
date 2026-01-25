package com.gege.activityfindermobile.utils;

/**
 * Utility class for formatting distances based on user preference (km or miles).
 */
public class DistanceFormatter {

    private static final double KM_TO_MILES = 0.621371;

    /**
     * Formats a distance value based on the user's unit preference.
     *
     * @param distanceKm    The distance in kilometers
     * @param useKilometers true to display in kilometers, false for miles
     * @return Formatted distance string (e.g., "5.2 km" or "3.2 mi")
     */
    public static String format(Double distanceKm, boolean useKilometers) {
        if (distanceKm == null || distanceKm <= 0) {
            return null;
        }

        if (useKilometers) {
            return String.format("%.1f km", distanceKm);
        } else {
            double miles = distanceKm * KM_TO_MILES;
            return String.format("%.1f mi", miles);
        }
    }

    /**
     * Converts kilometers to miles.
     *
     * @param km Distance in kilometers
     * @return Distance in miles
     */
    public static double kmToMiles(double km) {
        return km * KM_TO_MILES;
    }

    /**
     * Converts miles to kilometers.
     *
     * @param miles Distance in miles
     * @return Distance in kilometers
     */
    public static double milesToKm(double miles) {
        return miles / KM_TO_MILES;
    }
}
