package com.gege.activityfindermobile.config;

/**
 * Centralized configuration for API keys and endpoints Store all sensitive API keys and
 * configuration here
 */
public class ApiConfig {
    // Google Places API Key
    // Get this from: https://console.cloud.google.com/
    // Steps:
    // 1. Create a Google Cloud Project
    // 2. Enable Places API, Maps API, and Location Services
    // 3. Create an API Key credential
    // 4. Restrict to Android apps and add your app's SHA-1 fingerprint
    public static final String GOOGLE_PLACES_API_KEY = "AIzaSyCloP6U5KXPn454wevx1XQjDTeT8WQgIwY";

    // Private constructor to prevent instantiation
    private ApiConfig() {}
}
