package com.gege.activityfindermobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/** Manages SharedPreferences for storing user session data */
@Singleton
public class SharedPreferencesManager {
    private final SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferencesManager(@ApplicationContext Context context) {
        this.sharedPreferences =
                context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Save user ID */
    public void saveUserId(Long userId) {
        sharedPreferences.edit().putLong(Constants.KEY_USER_ID, userId).apply();
    }

    /** Get user ID */
    public Long getUserId() {
        long userId = sharedPreferences.getLong(Constants.KEY_USER_ID, -1L);
        return userId != -1L ? userId : null;
    }

    /** Save user token */
    public void saveUserToken(String token) {
        sharedPreferences.edit().putString(Constants.KEY_USER_TOKEN, token).apply();
    }

    /** Get user token */
    public String getUserToken() {
        return sharedPreferences.getString(Constants.KEY_USER_TOKEN, null);
    }

    /** Save refresh token */
    public void saveRefreshToken(String refreshToken) {
        sharedPreferences.edit().putString(Constants.KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    /** Get refresh token */
    public String getRefreshToken() {
        return sharedPreferences.getString(Constants.KEY_REFRESH_TOKEN, null);
    }

    /** Save user email */
    public void saveUserEmail(String email) {
        sharedPreferences.edit().putString(Constants.KEY_USER_EMAIL, email).apply();
    }

    /** Get user email */
    public String getUserEmail() {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, null);
    }

    /** Set login status */
    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    /** Check if user is logged in */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    /** Save user session (login) - now includes refresh token and email */
    public void saveUserSession(Long userId, String accessToken, String refreshToken, String email) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        sharedPreferences
                .edit()
                .putLong(Constants.KEY_USER_ID, userId)
                .putString(Constants.KEY_USER_TOKEN, accessToken != null ? accessToken : "")
                .putString(Constants.KEY_REFRESH_TOKEN, refreshToken != null ? refreshToken : "")
                .putString(Constants.KEY_USER_EMAIL, email != null ? email : "")
                .putBoolean(Constants.KEY_IS_LOGGED_IN, true)
                .apply();
    }

    /** Save user session (login) - without email */
    public void saveUserSession(Long userId, String accessToken, String refreshToken) {
        saveUserSession(userId, accessToken, refreshToken, null);
    }

    /** Legacy method for backward compatibility */
    @Deprecated
    public void saveUserSession(Long userId, String token) {
        saveUserSession(userId, token, null);
    }

    /** Clear user session (logout) */
    public void clearUserSession() {
        sharedPreferences
                .edit()
                .remove(Constants.KEY_USER_ID)
                .remove(Constants.KEY_USER_TOKEN)
                .remove(Constants.KEY_REFRESH_TOKEN)
                .remove(Constants.KEY_USER_EMAIL)
                .putBoolean(Constants.KEY_IS_LOGGED_IN, false)
                .apply();
    }

    /** Clear user data - alias for clearUserSession */
    public void clearUserData() {
        clearUserSession();
    }

    /** Clear all preferences */
    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    /** Save a boolean value */
    public void saveBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    /** Alias for saveBoolean */
    public void putBoolean(String key, boolean value) {
        saveBoolean(key, value);
    }

    /** Get a boolean value with a default */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /** Save a string value */
    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    /** Get a string value with a default */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
}
