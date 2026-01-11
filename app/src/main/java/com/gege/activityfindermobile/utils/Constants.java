package com.gege.activityfindermobile.utils;

public class Constants {
    // Change this to your backend URL
    public static final String BASE_URL = "http://10.0.2.2:8080/"; // Android emulator localhost
    // public static final String BASE_URL = "https://af.zen-vy.com/";
    // For production: "https://your-domain.com/"

    // SharedPreferences keys
    public static final String PREF_NAME = "ActivityBuddyPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_TOKEN = "userToken";
    public static final String KEY_REFRESH_TOKEN = "refreshToken";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // Filter constants
    public static final int[] DISTANCE_FILTER_OPTIONS = {5, 10, 25, 50, 250}; // km
    public static final int DEFAULT_MAX_DISTANCE = 250; // km

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
