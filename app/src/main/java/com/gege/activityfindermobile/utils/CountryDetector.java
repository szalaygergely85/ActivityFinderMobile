package com.gege.activityfindermobile.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Utility class to detect user's country from their location coordinates.
 * Used to dynamically set country restrictions for Google Places API.
 */
public class CountryDetector {
    private static final String TAG = "CountryDetector";
    private static final String PREF_USER_COUNTRY = "user_country_code";
    private static final String DEFAULT_COUNTRY = ""; // Empty means no country restriction

    /**
     * Detects the country code from coordinates using reverse geocoding.
     *
     * @param context Android context
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param callback Callback with the ISO country code (e.g., "US", "HU", "DE")
     */
    public static void detectCountry(Context context, double latitude, double longitude,
                                     CountryCallback callback) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String countryCode = address.getCountryCode();

                    if (countryCode != null && !countryCode.isEmpty()) {
                        Log.d(TAG, "Detected country: " + countryCode);
                        // Save to preferences
                        saveCountryCode(context, countryCode);
                        // Callback on main thread
                        if (callback != null) {
                            android.os.Handler mainHandler = new android.os.Handler(
                                    context.getMainLooper());
                            mainHandler.post(() -> callback.onCountryDetected(countryCode));
                        }
                        return;
                    }
                }

                Log.w(TAG, "Could not detect country from location");
                if (callback != null) {
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> callback.onCountryDetected(getSavedCountryCode(context)));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error detecting country", e);
                if (callback != null) {
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> callback.onCountryDetected(getSavedCountryCode(context)));
                }
            }
        }).start();
    }

    /**
     * Saves the detected country code to SharedPreferences.
     */
    public static void saveCountryCode(Context context, String countryCode) {
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_USER_COUNTRY, countryCode)
                .apply();
    }

    /**
     * Gets the saved country code from SharedPreferences.
     *
     * @return Country code or empty string if not set
     */
    public static String getSavedCountryCode(Context context) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_USER_COUNTRY, DEFAULT_COUNTRY);
    }

    /**
     * Checks if a country code has been detected and saved.
     */
    public static boolean hasCountryCode(Context context) {
        String code = getSavedCountryCode(context);
        return code != null && !code.isEmpty();
    }

    /**
     * Callback interface for country detection.
     */
    public interface CountryCallback {
        void onCountryDetected(String countryCode);
    }
}
