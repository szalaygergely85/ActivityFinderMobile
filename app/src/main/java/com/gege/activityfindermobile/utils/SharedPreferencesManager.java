package com.gege.activityfindermobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Manages SharedPreferences for storing user session data
 */
@Singleton
public class SharedPreferencesManager {
    private final SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferencesManager(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(
                Constants.PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    /**
     * Save user ID
     */
    public void saveUserId(Long userId) {
        sharedPreferences.edit()
                .putLong(Constants.KEY_USER_ID, userId)
                .apply();
    }

    /**
     * Get user ID
     */
    public Long getUserId() {
        long userId = sharedPreferences.getLong(Constants.KEY_USER_ID, -1L);
        return userId != -1L ? userId : null;
    }

    /**
     * Save user token
     */
    public void saveUserToken(String token) {
        sharedPreferences.edit()
                .putString(Constants.KEY_USER_TOKEN, token)
                .apply();
    }

    /**
     * Get user token
     */
    public String getUserToken() {
        return sharedPreferences.getString(Constants.KEY_USER_TOKEN, null);
    }

    /**
     * Set login status
     */
    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit()
                .putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn)
                .apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    /**
     * Save user session (login)
     */
    public void saveUserSession(Long userId, String token) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        sharedPreferences.edit()
                .putLong(Constants.KEY_USER_ID, userId)
                .putString(Constants.KEY_USER_TOKEN, token != null ? token : "")
                .putBoolean(Constants.KEY_IS_LOGGED_IN, true)
                .apply();
    }

    /**
     * Clear user session (logout)
     */
    public void clearUserSession() {
        sharedPreferences.edit()
                .remove(Constants.KEY_USER_ID)
                .remove(Constants.KEY_USER_TOKEN)
                .putBoolean(Constants.KEY_IS_LOGGED_IN, false)
                .apply();
    }

    /**
     * Clear all preferences
     */
    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}
