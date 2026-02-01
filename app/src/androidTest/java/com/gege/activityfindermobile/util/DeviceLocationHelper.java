package com.gege.activityfindermobile.util;

import android.Manifest;
import android.app.UiAutomation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.TimeUnit;

/**
 * Helper class for getting device location in instrumentation tests.
 * Gets the device's current location and sets it in TestDataFactory
 * so that test activities are created near the device.
 */
public class DeviceLocationHelper {

    private static final String TAG = "DeviceLocationHelper";

    // Default fallback location (NYC)
    private static final double DEFAULT_LATITUDE = 40.7128;
    private static final double DEFAULT_LONGITUDE = -74.0060;

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    private double latitude = DEFAULT_LATITUDE;
    private double longitude = DEFAULT_LONGITUDE;
    private boolean locationAcquired = false;

    public DeviceLocationHelper() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Get the device's current location and set it in TestDataFactory.
     * If location cannot be acquired, sets a mock location on the device.
     * This should be called before creating test activities.
     *
     * @return true if location was acquired, false if using default
     */
    public boolean acquireLocationAndSetForTests() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Location permission not granted, setting mock location");
                setMockLocationOnDevice(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
                TestDataFactory.setTestLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
                return false;
            }

            // Get last known location (blocking call)
            Location location = Tasks.await(
                    fusedLocationClient.getLastLocation(),
                    5,
                    TimeUnit.SECONDS
            );

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                locationAcquired = true;
                Log.d(TAG, "Device location acquired: " + latitude + ", " + longitude);
            } else {
                Log.w(TAG, "Location is null, setting mock location on device");
                // Set mock location so both test and app use the same location
                setMockLocationOnDevice(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
            }

        } catch (Exception e) {
            Log.w(TAG, "Failed to get device location: " + e.getMessage() + ", setting mock location");
            setMockLocationOnDevice(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        }

        // Set the location in TestDataFactory
        TestDataFactory.setTestLocation(latitude, longitude);
        Log.d(TAG, "Test location set to: " + latitude + ", " + longitude);

        return locationAcquired;
    }

    /**
     * Set a mock location on the device using multiple approaches.
     * This ensures both the test and the app see the same location.
     */
    private void setMockLocationOnDevice(double lat, double lon) {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

        // Approach 1: Try to set location via settings command (for emulator)
        try {
            // This works on some emulators
            uiAutomation.executeShellCommand(
                    "settings put secure location_mode 3");  // High accuracy mode
            Thread.sleep(100);

            // Try to set location providers
            uiAutomation.executeShellCommand(
                    "settings put secure location_providers_allowed +gps,network");
            Thread.sleep(100);
        } catch (Exception e) {
            Log.w(TAG, "Could not set location settings: " + e.getMessage());
        }

        // Approach 2: Try appops to allow mock location
        try {
            String packageName = context.getPackageName();
            uiAutomation.executeShellCommand("appops set " + packageName + " android:mock_location allow");
            uiAutomation.executeShellCommand("appops set " + packageName + " MOCK_LOCATION allow");
            Thread.sleep(100);
        } catch (Exception e) {
            Log.w(TAG, "Could not set mock_location permission via shell: " + e.getMessage());
        }

        // Approach 3: Use LocationManager test provider
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            String provider = LocationManager.GPS_PROVIDER;

            try {
                locationManager.removeTestProvider(provider);
            } catch (Exception ignored) {}

            locationManager.addTestProvider(
                    provider,
                    false, false, false, false,
                    true, true, true,
                    Criteria.POWER_LOW, Criteria.ACCURACY_FINE
            );
            locationManager.setTestProviderEnabled(provider, true);

            // Set the mock location multiple times
            for (int i = 0; i < 3; i++) {
                Location mockLocation = new Location(provider);
                mockLocation.setLatitude(lat);
                mockLocation.setLongitude(lon);
                mockLocation.setAltitude(0);
                mockLocation.setAccuracy(1.0f);
                mockLocation.setTime(System.currentTimeMillis());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }
                locationManager.setTestProviderLocation(provider, mockLocation);
                Thread.sleep(200);
            }

            Log.d(TAG, "Mock location set via LocationManager: " + lat + ", " + lon);
            latitude = lat;
            longitude = lon;
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException - mock locations not enabled in developer settings: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Failed to set mock location: " + e.getMessage());
        }

        // Approach 4: Try FusedLocationProviderClient mock mode
        try {
            fusedLocationClient.setMockMode(true);
            Thread.sleep(100);

            Location mockLocation = new Location("fused");
            mockLocation.setLatitude(lat);
            mockLocation.setLongitude(lon);
            mockLocation.setAccuracy(1.0f);
            mockLocation.setTime(System.currentTimeMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }

            fusedLocationClient.setMockLocation(mockLocation);
            Log.d(TAG, "Mock location set via FusedLocationProviderClient: " + lat + ", " + lon);
        } catch (Exception e) {
            Log.w(TAG, "Could not set FusedLocationProviderClient mock: " + e.getMessage());
        }
    }

    /**
     * Get the acquired latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get the acquired longitude.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Check if location was successfully acquired from device.
     */
    public boolean isLocationAcquired() {
        return locationAcquired;
    }
}
