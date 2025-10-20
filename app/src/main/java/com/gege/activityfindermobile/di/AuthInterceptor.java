package com.gege.activityfindermobile.di;

import com.gege.activityfindermobile.utils.SharedPreferencesManager;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** OkHttp Interceptor that adds JWT Bearer token to all API requests */
public class AuthInterceptor implements Interceptor {
    private final SharedPreferencesManager prefsManager;

    @Inject
    public AuthInterceptor(SharedPreferencesManager prefsManager) {
        this.prefsManager = prefsManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Skip auth header for login and register endpoints
        String path = originalRequest.url().encodedPath();
        if (path.contains("/api/users/login")
                || path.contains("/api/users/register")
                || path.contains("/api/auth/login")
                || path.contains("/api/auth/register")) {
            return chain.proceed(originalRequest);
        }

        // Get token from SharedPreferences
        String token = prefsManager.getUserToken();

        // If no token available, proceed without Authorization header
        if (token == null || token.isEmpty()) {
            return chain.proceed(originalRequest);
        }

        // Add Authorization header with Bearer token
        Request authenticatedRequest =
                originalRequest.newBuilder().header("Authorization", "Bearer " + token).build();

        return chain.proceed(authenticatedRequest);
    }
}
