package com.gege.activityfindermobile.utils;

import android.Manifest;

import com.gege.activityfindermobile.utils.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

public class LocationManager {
    private static final float DEFAULT_RADIUS_KM = Constants.DEFAULT_MAX_DISTANCE;
    private static final int EARTH_RADIUS_KM = 6371;
    private static final String PREFS_NAME = "LocationPrefs";
    private static final String KEY_LAST_LATITUDE = "last_latitude";
    private static final String KEY_LAST_LONGITUDE = "last_longitude";
    private static final String KEY_HAS_LOCATION = "has_location";

    // Default fallback location (NYC) - only used if no stored location exists
    private static final double DEFAULT_LATITUDE = 40.7128;
    private static final double DEFAULT_LONGITUDE = -74.0060;

    private FusedLocationProviderClient fusedLocationClient;
    private Context context;

    public LocationManager(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Save location to SharedPreferences for fallback use
     */
    private void saveLocation(double latitude, double longitude) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(KEY_LAST_LATITUDE, (float) latitude)
                .putFloat(KEY_LAST_LONGITUDE, (float) longitude)
                .putBoolean(KEY_HAS_LOCATION, true)
                .apply();
        android.util.Log.d("LocationManager", "Saved location: " + latitude + ", " + longitude);
    }

    /**
     * Get stored location from SharedPreferences
     * @return double array [latitude, longitude] or null if no stored location
     */
    private double[] getStoredLocation() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_HAS_LOCATION, false)) {
            double lat = prefs.getFloat(KEY_LAST_LATITUDE, 0f);
            double lon = prefs.getFloat(KEY_LAST_LONGITUDE, 0f);
            return new double[]{lat, lon};
        }
        return null;
    }

    /**
     * Get current device location
     *
     * @param context Android context
     * @param callback Callback with location data
     */
    public void getCurrentLocation(Context context, LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Try stored location before failing
            useFallbackLocation(callback, "Location permission not granted");
            return;
        }

        // First try getCurrentLocation
        Task<Location> locationTask =
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null);

        locationTask.addOnSuccessListener(
                location -> {
                    if (location != null) {
                        // Save this location for future fallback
                        saveLocation(location.getLatitude(), location.getLongitude());
                        callback.onLocationReceived(
                                location.getLatitude(), location.getLongitude());
                    } else {
                        // Fallback: try getLastLocation
                        tryLastLocation(callback);
                    }
                });

        locationTask.addOnFailureListener(
                e -> {
                    // Fallback: try getLastLocation
                    tryLastLocation(callback);
                });
    }

    /**
     * Fallback method to try getting last known location
     */
    private void tryLastLocation(LocationCallback callback) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Save this location for future fallback
                        saveLocation(location.getLatitude(), location.getLongitude());
                        callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        // Use stored or default location
                        useFallbackLocation(callback, "No location available from device");
                    }
                })
                .addOnFailureListener(e -> {
                    // Use stored or default location
                    useFallbackLocation(callback, "Location retrieval failed: " + e.getMessage());
                });
    }

    /**
     * Use stored location from SharedPreferences, or default if none stored
     */
    private void useFallbackLocation(LocationCallback callback, String reason) {
        double[] stored = getStoredLocation();
        if (stored != null) {
            android.util.Log.w("LocationManager", reason + " - using stored location: " + stored[0] + ", " + stored[1]);
            callback.onLocationReceived(stored[0], stored[1]);
        } else {
            android.util.Log.w("LocationManager", reason + " - no stored location, using default (NYC)");
            callback.onLocationReceived(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        }
    }

    /**
     * Calculate distance between two points using Haversine formula
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                                * Math.cos(Math.toRadians(lat2))
                                * Math.sin(dLon / 2)
                                * Math.sin(dLon / 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Check if a location is within a specific radius
     *
     * @param userLat User latitude
     * @param userLon User longitude
     * @param activityLat Activity latitude
     * @param activityLon Activity longitude
     * @param radiusKm Radius in kilometers
     * @return True if activity is within radius
     */
    public static boolean isWithinRadius(
            double userLat,
            double userLon,
            double activityLat,
            double activityLon,
            float radiusKm) {
        double distance = calculateDistance(userLat, userLon, activityLat, activityLon);
        return distance <= radiusKm;
    }

    /**
     * Check if a location is within default radius (10 km)
     *
     * @param userLat User latitude
     * @param userLon User longitude
     * @param activityLat Activity latitude
     * @param activityLon Activity longitude
     * @return True if activity is within default radius
     */
    public static boolean isWithinDefaultRadius(
            double userLat, double userLon, double activityLat, double activityLon) {
        return isWithinRadius(userLat, userLon, activityLat, activityLon, DEFAULT_RADIUS_KM);
    }

    /**
     * Get distance between two locations
     *
     * @param userLat User latitude
     * @param userLon User longitude
     * @param activityLat Activity latitude
     * @param activityLon Activity longitude
     * @return Distance in kilometers, rounded to 1 decimal place
     */
    public static double getDistance(
            double userLat, double userLon, double activityLat, double activityLon) {
        return Math.round(calculateDistance(userLat, userLon, activityLat, activityLon) * 10.0)
                / 10.0;
    }

    /** Callback interface for location operations */
    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);

        void onError(String errorMessage);
    }
}
