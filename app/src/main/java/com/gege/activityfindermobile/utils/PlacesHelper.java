package com.gege.activityfindermobile.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Helper class for location selection using Google Maps Picker Provides a simple interface for
 * users to select a location with coordinates
 */
public class PlacesHelper {
    private static final String TAG = "PlacesHelper";
    private static final int LOCATION_PICKER_REQUEST_CODE = 1;

    /**
     * Initialize Places/Location services. Call before using location features.
     *
     * @param context Android context
     * @param apiKey Google Places API Key (not required for basic location functionality)
     */
    public static void initializePlaces(Context context, String apiKey) {
        // Validation of API key structure
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_GOOGLE_PLACES_API_KEY")) {
            Log.w(TAG, "Warning: Google Places API Key not configured properly");
            Toast.makeText(
                            context,
                            "Please configure Google Places API Key in ApiConfig.java",
                            Toast.LENGTH_LONG)
                    .show();
        } else {
            Log.d(TAG, "Places API Key configured successfully");
        }
    }

    /**
     * Create an intent for location selection. This uses a custom location picker dialog or Google
     * Maps.
     *
     * @param context Android context
     * @return Intent for location picker activity
     */
    public static Intent createAutocompleteIntent(Context context) {
        // For now, we'll use a simplified approach with a location picker
        // In production, you would integrate with Google Places API directly
        // or use a third-party location picker library

        Intent intent = new Intent(context, LocationPickerActivity.class);
        return intent;
    }

    /**
     * Parse autocomplete activity result
     *
     * @param resultCode Result code from onActivityResult
     * @param data Intent data from onActivityResult
     * @param callback Callback with place data
     */
    public static void handleAutocompleteResult(
            int resultCode, Intent data, PlaceCallback callback) {
        if (resultCode == LocationPickerActivity.RESULT_LOCATION_SELECTED && data != null) {
            String placeName = data.getStringExtra("place_name");
            String address = data.getStringExtra("address");
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);

            if (latitude != 0.0 && longitude != 0.0) {
                callback.onPlaceSelected(placeName, address, latitude, longitude);
            } else {
                callback.onError("Unable to get coordinates for this location");
            }
        } else if (resultCode == LocationPickerActivity.RESULT_CANCELED) {
            callback.onError("User cancelled location selection");
        } else {
            callback.onError("Location selection failed");
        }
    }

    /**
     * Format location name with coordinates
     *
     * @param placeName Place name
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Formatted location string
     */
    public static String formatLocation(String placeName, double latitude, double longitude) {
        return String.format("%s (%.4f, %.4f)", placeName, latitude, longitude);
    }

    /**
     * Extract only the city/area name from full address
     *
     * @param address Full address from Places API
     * @return Simplified location name
     */
    public static String extractLocationName(String address) {
        if (address == null || address.isEmpty()) {
            return "Unknown Location";
        }

        // Get the first part (usually the most specific location)
        String[] parts = address.split(",");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        return address;
    }

    /** Callback interface for place selection */
    public interface PlaceCallback {
        /**
         * Called when a place is successfully selected
         *
         * @param placeName Name of the selected place
         * @param address Full address of the place
         * @param latitude Latitude coordinate
         * @param longitude Longitude coordinate
         */
        void onPlaceSelected(String placeName, String address, double latitude, double longitude);

        /**
         * Called when an error occurs during place selection
         *
         * @param errorMessage Error message describing what went wrong
         */
        void onError(String errorMessage);
    }
}
