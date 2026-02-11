package com.gege.activityfindermobile.di;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gege.activityfindermobile.data.api.UserApiService;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.dto.RefreshTokenRequest;
import com.gege.activityfindermobile.ui.main.MainActivity;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** OkHttp Interceptor that adds JWT Bearer token to all API requests and handles auth errors */
public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private final SharedPreferencesManager prefsManager;
    private final Context context;
    private final dagger.Lazy<UserApiService> userApiServiceLazy;

    @Inject
    public AuthInterceptor(
            SharedPreferencesManager prefsManager,
            @ApplicationContext Context context,
            dagger.Lazy<UserApiService> userApiServiceLazy) {
        this.prefsManager = prefsManager;
        this.context = context;
        this.userApiServiceLazy = userApiServiceLazy;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Skip auth header for public endpoints
        String path = originalRequest.url().encodedPath();
        if (path.contains("/api/users/login")
                || path.contains("/api/users/register")
                || path.contains("/api/auth/login")
                || path.contains("/api/auth/register")
                || path.contains("/api/users/refresh-token")
                || path.contains("/api/categories")) {
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

        Response response = chain.proceed(authenticatedRequest);

        // Handle 401 Unauthorized or 403 Forbidden - try to refresh token
        if (response.code() == 401 || response.code() == 403) {
            Log.w(TAG, "Received " + response.code() + " - Attempting to refresh token");

            synchronized (this) {
                // Close the failed response
                response.close();

                // Try to refresh the token
                String refreshToken = prefsManager.getRefreshToken();
                if (refreshToken != null && !refreshToken.isEmpty()) {
                    try {
                        // Get the UserApiService lazily to avoid circular dependency
                        UserApiService userApiService = userApiServiceLazy.get();

                        // Call refresh token endpoint with request body
                        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
                        retrofit2.Response<LoginResponse> refreshResponse =
                                userApiService.refreshToken(refreshRequest).execute();

                        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                            LoginResponse loginResponse = refreshResponse.body();
                            Log.i(TAG, "Token refreshed successfully");

                            // Save new tokens
                            prefsManager.saveUserToken(loginResponse.getAccessToken());
                            if (loginResponse.getRefreshToken() != null) {
                                prefsManager.saveRefreshToken(loginResponse.getRefreshToken());
                            }

                            // Retry the original request with new token
                            Request newRequest =
                                    originalRequest
                                            .newBuilder()
                                            .header(
                                                    "Authorization",
                                                    "Bearer " + loginResponse.getAccessToken())
                                            .build();
                            return chain.proceed(newRequest);
                        } else {
                            Log.e(TAG, "Token refresh failed: " + refreshResponse.code());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error refreshing token", e);
                    }
                }

                // If refresh failed or no refresh token, log out
                Log.w(TAG, "Token refresh failed, logging out user");
                prefsManager.clearUserData();

                // Restart MainActivity which will redirect to LoginFragment since user is logged
                // out
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

                // Return the original failed response
                return response;
            }
        }

        return response;
    }
}
