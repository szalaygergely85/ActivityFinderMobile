package com.gege.activityfindermobile.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

public class LocationManager {
    private static final float DEFAULT_RADIUS_KM = 250f;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int EARTH_RADIUS_KM = 6371;

    public LocationManager(Context context) {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
            callback.onError("Location permission not granted");
            return;
        }

        Task<Location> locationTask =
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null);

        locationTask.addOnSuccessListener(
                location -> {
                    if (location != null) {
                        callback.onLocationReceived(
                                location.getLatitude(), location.getLongitude());
                    } else {
                        callback.onError("Unable to get current location");
                    }
                });

        locationTask.addOnFailureListener(
                e -> callback.onError("Location retrieval failed: " + e.getMessage()));
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
