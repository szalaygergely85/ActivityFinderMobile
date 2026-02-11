package com.gege.activityfindermobile.util;

import android.util.Log;

import com.gege.activityfindermobile.data.api.ActivityApiService;
import com.gege.activityfindermobile.data.api.CoverImageApiService;
import com.gege.activityfindermobile.data.api.MessageApiService;
import com.gege.activityfindermobile.data.api.ParticipantApiService;
import com.gege.activityfindermobile.data.api.ParticipantStatusUpdateRequest;
import com.gege.activityfindermobile.data.api.ReportApiService;
import com.gege.activityfindermobile.data.api.UserApiService;
import com.gege.activityfindermobile.data.api.UserPhotoApiService;
import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.dto.ExpressInterestRequest;
import com.gege.activityfindermobile.data.dto.LoginRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.dto.MessageRequest;
import com.gege.activityfindermobile.data.dto.ReportCountResponse;
import com.gege.activityfindermobile.data.dto.ReportRequest;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.dto.UserRegistrationRequest;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.model.ActivityMessage;
import com.gege.activityfindermobile.data.model.CoverImage;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.model.Report;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.model.UserPhoto;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.gege.activityfindermobile.utils.Constants;

/**
 * Synchronous API helper for Android instrumentation tests.
 * All methods block until the API call completes.
 * Uses the same BASE_URL as the app to ensure test users are created on the same server.
 */
public class TestApiHelper {

    private static final String TAG = "TestApiHelper";
    // Use the same BASE_URL as the app to ensure test data is on the same server
    private static final String BASE_URL = Constants.BASE_URL;

    // API Services
    private final UserApiService userApiService;
    private final ActivityApiService activityApiService;
    private final CoverImageApiService coverImageApiService;
    private final ParticipantApiService participantApiService;
    private final ReportApiService reportApiService;
    private final UserPhotoApiService userPhotoApiService;
    private final MessageApiService messageApiService;

    // Session state
    private String currentAccessToken;
    private Long currentUserId;

    // Participant status constants (match backend ParticipantStatus enum)
    public static final String PARTICIPANT_STATUS_PENDING = "INTERESTED";
    public static final String PARTICIPANT_STATUS_ACCEPTED = "ACCEPTED";
    public static final String PARTICIPANT_STATUS_DECLINED = "DECLINED";
    public static final String PARTICIPANT_STATUS_REMOVED = "DECLINED"; // Same as declined

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
        coverImageApiService = retrofit.create(CoverImageApiService.class);
        participantApiService = retrofit.create(ParticipantApiService.class);
        reportApiService = retrofit.create(ReportApiService.class);
        userPhotoApiService = retrofit.create(UserPhotoApiService.class);
        messageApiService = retrofit.create(MessageApiService.class);
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

    // ==================== Cover Image Operations ====================

    /**
     * Get all available cover images synchronously.
     *
     * @return List of CoverImage objects, or null on failure
     */
    public List<CoverImage> getAllCoverImages() {
        try {
            Response<List<CoverImage>> response = coverImageApiService.getAllCoverImages().execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Found " + response.body().size() + " cover images");
                return response.body();
            } else {
                Log.e(TAG, "Failed to get cover images: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting cover images: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Upload a cover image synchronously.
     *
     * @param imageFile The image file to upload
     * @param displayName The display name for the cover image
     * @return CoverImage object, or null on failure
     */
    public CoverImage uploadCoverImage(java.io.File imageFile, String displayName) {
        if (imageFile == null || !imageFile.exists()) {
            Log.e(TAG, "Image file not found for cover upload");
            return null;
        }
        try {
            okhttp3.RequestBody requestFile =
                    okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/png"), imageFile);
            okhttp3.MultipartBody.Part filePart =
                    okhttp3.MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
            okhttp3.RequestBody displayNamePart =
                    okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), displayName);

            Response<CoverImage> response =
                    coverImageApiService.uploadCoverImage(filePart, displayNamePart).execute();
            if (response.isSuccessful() && response.body() != null) {
                CoverImage cover = response.body();
                Log.d(TAG, "Cover image uploaded: " + cover.getId() + " - " + cover.getImageUrl());
                return cover;
            } else {
                String errorBody = response.errorBody() != null
                        ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Failed to upload cover image: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error uploading cover image: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== Activity Update Operations ====================

    /**
     * Update an activity synchronously.
     *
     * @return Updated Activity object, or null on failure
     */
    public Activity updateActivity(Long activityId, ActivityCreateRequest request) {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return null;
        }
        try {
            Response<Activity> response =
                    activityApiService.updateActivity(activityId, currentUserId, request).execute();
            if (response.isSuccessful() && response.body() != null) {
                Activity activity = response.body();
                Log.d(TAG, "Activity updated successfully: " + activity.getId());
                return activity;
            } else {
                String errorBody =
                        response.errorBody() != null
                                ? response.errorBody().string()
                                : "Unknown error";
                Log.e(TAG, "Failed to update activity: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error updating activity: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== User Profile Operations ====================

    /**
     * Get user by ID synchronously.
     *
     * @return User object, or null on failure
     */
    public User getUserById(Long userId) {
        try {
            Response<User> response = userApiService.getUserById(userId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get user: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting user: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get current user profile.
     *
     * @return User object, or null on failure
     */
    public User getCurrentUserProfile() {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return null;
        }
        return getUserById(currentUserId);
    }

    /**
     * Update user profile synchronously.
     *
     * @return Updated User object, or null on failure
     */
    public User updateUserProfile(UserProfileUpdateRequest request) {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return null;
        }
        try {
            Response<User> response =
                    userApiService
                            .updateUserProfile(currentUserId, currentUserId, request)
                            .execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Profile updated successfully");
                return response.body();
            } else {
                String errorBody =
                        response.errorBody() != null
                                ? response.errorBody().string()
                                : "Unknown error";
                Log.e(TAG, "Failed to update profile: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error updating profile: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== User Photo Operations ====================

    /**
     * Get current user's photos synchronously.
     *
     * @return List of UserPhoto objects, or null on failure
     */
    public List<UserPhoto> getMyPhotos() {
        try {
            Response<List<UserPhoto>> response = userPhotoApiService.getMyPhotos().execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get photos: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting photos: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get another user's photos synchronously.
     *
     * @return List of UserPhoto objects, or null on failure
     */
    public List<UserPhoto> getUserPhotos(Long userId) {
        try {
            Response<List<UserPhoto>> response =
                    userPhotoApiService.getUserPhotos(userId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get user photos: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting user photos: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Set a photo as profile picture synchronously.
     *
     * @return Updated UserPhoto, or null on failure
     */
    public UserPhoto setPhotoAsProfile(Long photoId) {
        try {
            Response<UserPhoto> response =
                    userPhotoApiService.setPhotoAsProfile(photoId).execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Photo set as profile: " + photoId);
                return response.body();
            } else {
                Log.e(TAG, "Failed to set photo as profile: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error setting photo as profile: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Delete a photo synchronously.
     *
     * @return true if successful, false otherwise
     */
    public boolean deletePhoto(Long photoId) {
        try {
            Response<Void> response = userPhotoApiService.deletePhoto(photoId).execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "Photo deleted: " + photoId);
                return true;
            } else {
                Log.e(TAG, "Failed to delete photo: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error deleting photo: " + e.getMessage(), e);
            return false;
        }
    }

    // ==================== Participant Operations ====================

    /**
     * Express interest in an activity synchronously.
     *
     * @return Participant object, or null on failure
     */
    public Participant expressInterest(Long activityId, boolean isFriend) {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return null;
        }
        try {
            ExpressInterestRequest request = new ExpressInterestRequest(isFriend);
            Response<Participant> response =
                    participantApiService
                            .expressInterest(activityId, currentUserId, request)
                            .execute();
            if (response.isSuccessful() && response.body() != null) {
                Participant participant = response.body();
                Log.d(
                        TAG,
                        "Interest expressed for activity "
                                + activityId
                                + ", participant ID: "
                                + participant.getId());
                return participant;
            } else {
                String errorBody =
                        response.errorBody() != null
                                ? response.errorBody().string()
                                : "Unknown error";
                Log.e(TAG, "Failed to express interest: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error expressing interest: " + e.getMessage(), e);
            return null;
        }
    }

    /** Express interest in an activity (not as friend). */
    public Participant expressInterest(Long activityId) {
        return expressInterest(activityId, false);
    }

    /**
     * Update participant status synchronously (for activity creator).
     *
     * @param participantId The participant record ID
     * @param status The new status: ACCEPTED, DECLINED, or REMOVED
     * @return Updated Participant, or null on failure
     */
    public Participant updateParticipantStatus(Long participantId, String status) {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return null;
        }
        try {
            ParticipantStatusUpdateRequest request = new ParticipantStatusUpdateRequest(status);
            Response<Participant> response =
                    participantApiService
                            .updateParticipantStatus(participantId, currentUserId, request)
                            .execute();
            if (response.isSuccessful() && response.body() != null) {
                Participant participant = response.body();
                Log.d(TAG, "Participant " + participantId + " status updated to: " + status);
                return participant;
            } else {
                String errorBody =
                        response.errorBody() != null
                                ? response.errorBody().string()
                                : "Unknown error";
                Log.e(
                        TAG,
                        "Failed to update participant status: "
                                + response.code()
                                + " - "
                                + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error updating participant status: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get activity participants synchronously.
     *
     * @return List of Participant objects, or null on failure
     */
    public List<Participant> getActivityParticipants(Long activityId) {
        try {
            Response<List<Participant>> response =
                    participantApiService.getActivityParticipants(activityId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get participants: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting participants: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get interested users for an activity (pending participants).
     *
     * @return List of Participant objects, or null on failure
     */
    public List<Participant> getInterestedUsers(Long activityId) {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return null;
        }
        try {
            Response<List<Participant>> response =
                    participantApiService.getInterestedUsers(activityId, currentUserId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get interested users: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting interested users: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get current user's participations.
     *
     * @return List of Participant objects, or null on failure
     */
    public List<Participant> getMyParticipations() {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return null;
        }
        try {
            Response<List<Participant>> response =
                    participantApiService.getMyParticipations(currentUserId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get participations: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting participations: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Leave an activity synchronously.
     *
     * @return true if successful, false otherwise
     */
    public boolean leaveActivity(Long activityId) {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - login first");
            return false;
        }
        try {
            Response<Void> response =
                    participantApiService.leaveActivity(activityId, currentUserId).execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "Left activity: " + activityId);
                return true;
            } else {
                Log.e(TAG, "Failed to leave activity: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error leaving activity: " + e.getMessage(), e);
            return false;
        }
    }

    // ==================== Report Operations ====================

    /**
     * Submit a report synchronously.
     *
     * @return Report object, or null on failure
     */
    public Report submitReport(ReportRequest request) {
        try {
            Response<Report> response = reportApiService.submitReport(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                Report report = response.body();
                Log.d(TAG, "Report submitted: " + report.getId());
                return report;
            } else {
                String errorBody =
                        response.errorBody() != null
                                ? response.errorBody().string()
                                : "Unknown error";
                Log.e(TAG, "Failed to submit report: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error submitting report: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Report an activity.
     *
     * @return Report object, or null on failure
     */
    public Report reportActivity(Long activityId, String reason) {
        return submitReport(ReportRequest.forActivity(activityId, reason));
    }

    /**
     * Report a user.
     *
     * @return Report object, or null on failure
     */
    public Report reportUser(Long userId, String reason) {
        return submitReport(ReportRequest.forUser(userId, reason));
    }

    /**
     * Report a message.
     *
     * @return Report object, or null on failure
     */
    public Report reportMessage(Long messageId, String reason) {
        return submitReport(ReportRequest.forMessage(messageId, reason));
    }

    /**
     * Get my reports synchronously.
     *
     * @return List of Report objects, or null on failure
     */
    public List<Report> getMyReports() {
        try {
            Response<List<Report>> response = reportApiService.getMyReports().execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get my reports: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting my reports: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get activity report count synchronously.
     *
     * @return Report count, or -1 on failure
     */
    public int getActivityReportCount(Long activityId) {
        try {
            Response<ReportCountResponse> response =
                    reportApiService.getActivityReportCount(activityId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().getReportCount();
            } else {
                Log.e(TAG, "Failed to get activity report count: " + response.code());
                return -1;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting activity report count: " + e.getMessage(), e);
            return -1;
        }
    }

    // ==================== Message Operations ====================

    /**
     * Send a message in an activity chat synchronously.
     *
     * @return ActivityMessage object, or null on failure
     */
    public ActivityMessage sendMessage(Long activityId, String content) {
        try {
            MessageRequest request = new MessageRequest(content);
            Response<ActivityMessage> response =
                    messageApiService.sendMessage(activityId, request).execute();
            if (response.isSuccessful() && response.body() != null) {
                ActivityMessage message = response.body();
                Log.d(TAG, "Message sent: " + message.getId());
                return message;
            } else {
                String errorBody =
                        response.errorBody() != null
                                ? response.errorBody().string()
                                : "Unknown error";
                Log.e(TAG, "Failed to send message: " + response.code() + " - " + errorBody);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error sending message: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get messages for an activity synchronously.
     *
     * @return List of ActivityMessage objects, or null on failure
     */
    public List<ActivityMessage> getMessages(Long activityId) {
        try {
            Response<List<ActivityMessage>> response =
                    messageApiService.getMessages(activityId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to get messages: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting messages: " + e.getMessage(), e);
            return null;
        }
    }
}
