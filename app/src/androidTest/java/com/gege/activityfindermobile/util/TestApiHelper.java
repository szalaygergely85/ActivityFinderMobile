package com.gege.activityfindermobile.util;

import android.util.Log;

import com.gege.activityfindermobile.data.api.ActivityApiService;
import com.gege.activityfindermobile.data.api.UserApiService;
import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.dto.LoginRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.dto.UserRegistrationRequest;
import com.gege.activityfindermobile.data.model.Activity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Synchronous API helper for Android instrumentation tests.
 * All methods block until the API call completes.
 */
public class TestApiHelper {

    private static final String TAG = "TestApiHelper";
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // Android emulator localhost

    private final UserApiService userApiService;
    private final ActivityApiService activityApiService;

    private String currentAccessToken;
    private Long currentUserId;

    public TestApiHelper() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request.Builder requestBuilder = original.newBuilder();

                    // Add auth token if available
                    if (currentAccessToken != null) {
                        requestBuilder.header("Authorization", "Bearer " + currentAccessToken);
                    }

                    return chain.proceed(requestBuilder.build());
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        userApiService = retrofit.create(UserApiService.class);
        activityApiService = retrofit.create(ActivityApiService.class);
    }

    // ==================== User Operations ====================

    /**
     * Register a new user synchronously.
     * @return LoginResponse with tokens and user info, or null on failure
     */
    public LoginResponse createUser(String fullName, String email, String password, String birthDate) {
        UserRegistrationRequest request = new UserRegistrationRequest(fullName, email, password, birthDate);

        try {
            Response<LoginResponse> response = userApiService.registerUser(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                LoginResponse loginResponse = response.body();
                currentAccessToken = loginResponse.getAccessToken();
                currentUserId = loginResponse.getUserId();
                Log.d(TAG, "User created successfully: " + email + ", userId: " + currentUserId);
                return loginResponse;
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Failed to create user: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error creating user: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Login user synchronously.
     * @return LoginResponse with tokens, or null on failure
     */
    public LoginResponse login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);

        try {
            Response<LoginResponse> response = userApiService.loginUser(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                LoginResponse loginResponse = response.body();
                currentAccessToken = loginResponse.getAccessToken();
                currentUserId = loginResponse.getUserId();
                Log.d(TAG, "Login successful: " + email + ", userId: " + currentUserId);
                return loginResponse;
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Failed to login: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error during login: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Delete/deactivate user synchronously.
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(Long userId) {
        try {
            Response<Void> response = userApiService.deactivateUser(userId).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "User deleted successfully: " + userId);
                return true;
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Failed to delete user: " + response.code() + " - " + errorBody);
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error deleting user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete current logged-in user.
     * @return true if successful, false otherwise
     */
    public boolean deleteCurrentUser() {
        if (currentUserId == null) {
            Log.e(TAG, "No current user to delete");
            return false;
        }
        return deleteUser(currentUserId);
    }

    // ==================== Activity Operations ====================

    /**
     * Create an activity synchronously.
     * @return Activity object, or null on failure
     */
    public Activity createActivity(Long creatorId, ActivityCreateRequest request) {
        try {
            Response<Activity> response = activityApiService.createActivity(creatorId, request).execute();

            if (response.isSuccessful() && response.body() != null) {
                Activity activity = response.body();
                Log.d(TAG, "Activity created successfully: " + activity.getId() + " - " + activity.getTitle());
                return activity;
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Failed to create activity: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error creating activity: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Create an activity for current logged-in user.
     * @return Activity object, or null on failure
     */
    public Activity createActivity(ActivityCreateRequest request) {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return null;
        }
        return createActivity(currentUserId, request);
    }

    /**
     * Create an activity with basic parameters.
     * @return Activity object, or null on failure
     */
    public Activity createActivity(String title, String description, String activityDate,
                                   String location, Integer totalSpots, String category) {
        ActivityCreateRequest request = new ActivityCreateRequest(
                title, description, activityDate, location, totalSpots, category);
        return createActivity(request);
    }

    /**
     * Delete/cancel an activity synchronously.
     * @return true if successful, false otherwise
     */
    public boolean deleteActivity(Long activityId, Long userId) {
        try {
            Response<Void> response = activityApiService.cancelActivity(activityId, userId).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Activity deleted successfully: " + activityId);
                return true;
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Failed to delete activity: " + response.code() + " - " + errorBody);
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error deleting activity: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete activity using current logged-in user.
     * @return true if successful, false otherwise
     */
    public boolean deleteActivity(Long activityId) {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return false;
        }
        return deleteActivity(activityId, currentUserId);
    }

    /**
     * Get activity by ID synchronously.
     * @return Activity object, or null on failure
     */
    public Activity getActivity(Long activityId) {
        try {
            Response<Activity> response = activityApiService.getActivityById(activityId).execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get activity: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting activity: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get nearby activities synchronously.
     * @return List of activities, or null on failure
     */
    public java.util.List<Activity> getNearbyActivities(double latitude, double longitude, float radiusKm) {
        try {
            Response<java.util.List<Activity>> response = activityApiService
                    .getNearbyActivities(latitude, longitude, radiusKm).execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Found " + response.body().size() + " nearby activities");
                return response.body();
            } else {
                Log.e(TAG, "Failed to get nearby activities: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting nearby activities: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get activities by category synchronously.
     * @return List of activities, or null on failure
     */
    public java.util.List<Activity> getActivitiesByCategory(String category) {
        try {
            Response<java.util.List<Activity>> response = activityApiService
                    .getActivitiesByCategory(category).execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Found " + response.body().size() + " activities in category: " + category);
                return response.body();
            } else {
                Log.e(TAG, "Failed to get activities by category: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting activities by category: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get all open activities synchronously.
     * @return List of activities, or null on failure
     */
    public java.util.List<Activity> getAllOpenActivities() {
        try {
            Response<java.util.List<Activity>> response = activityApiService
                    .getAllOpenActivities().execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Found " + response.body().size() + " open activities");
                return response.body();
            } else {
                Log.e(TAG, "Failed to get open activities: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting open activities: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== Getters ====================

    public String getCurrentAccessToken() {
        return currentAccessToken;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentAccessToken(String token) {
        this.currentAccessToken = token;
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
    }

    /**
     * Clear current session.
     */
    public void clearSession() {
        currentAccessToken = null;
        currentUserId = null;
    }

    /**
     * Wait for specified milliseconds.
     * Useful for waiting between operations that need backend processing time.
     */
    public void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Wait for a short time (500ms) - use between dependent operations.
     */
    public void waitShort() {
        waitMillis(500);
    }

    /**
     * Wait for medium time (1000ms) - use after create operations before querying.
     */
    public void waitMedium() {
        waitMillis(1000);
    }

    /**
     * Login with retry - useful when login might fail due to timing.
     * @param maxRetries Maximum number of retry attempts
     * @param delayBetweenRetries Delay in ms between retries
     * @return LoginResponse or null if all retries fail
     */
    public LoginResponse loginWithRetry(String email, String password, int maxRetries, long delayBetweenRetries) {
        for (int i = 0; i <= maxRetries; i++) {
            LoginResponse response = login(email, password);
            if (response != null) {
                return response;
            }
            if (i < maxRetries) {
                Log.d(TAG, "Login attempt " + (i + 1) + " failed, retrying in " + delayBetweenRetries + "ms...");
                waitMillis(delayBetweenRetries);
            }
        }
        return null;
    }

    /**
     * Login with default retry settings (3 retries, 500ms delay).
     */
    public LoginResponse loginWithRetry(String email, String password) {
        return loginWithRetry(email, password, 3, 500);
    }
}
