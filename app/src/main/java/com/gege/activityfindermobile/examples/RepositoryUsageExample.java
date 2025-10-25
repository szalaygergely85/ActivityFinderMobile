package com.gege.activityfindermobile.examples;

import android.util.Log;

import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.dto.ExpressInterestRequest;
import com.gege.activityfindermobile.data.dto.LoginRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.dto.ReviewRequest;
import com.gege.activityfindermobile.data.dto.UserRegistrationRequest;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.model.Review;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.data.repository.ReviewRepository;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;

import java.util.List;

import javax.inject.Inject;

/**
 * Example class demonstrating how to use the repositories This is for reference only - use these
 * patterns in your Activities/Fragments/ViewModels
 */
public class RepositoryUsageExample {
    private static final String TAG = "RepositoryExample";

    @Inject UserRepository userRepository;

    @Inject ActivityRepository activityRepository;

    @Inject ParticipantRepository participantRepository;

    @Inject ReviewRepository reviewRepository;

    @Inject SharedPreferencesManager prefsManager;

    /** Example: Register a new user */
    public void exampleRegisterUser() {
        UserRegistrationRequest request =
                new UserRegistrationRequest("John Doe", "john@example.com", "password123");

        userRepository.registerUser(
                request,
                new ApiCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse loginResponse) {
                        Long userId = loginResponse.getUserId();
                        Log.d(TAG, "Registration successful! User ID: " + userId);
                        // Save user session
                        prefsManager.saveUserSession(userId, loginResponse.getAccessToken());
                        // Navigate to home screen or show success message
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Registration failed: " + errorMessage);
                        // Show error message to user
                    }
                });
    }

    /** Example: Login user */
    public void exampleLoginUser() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");

        userRepository.loginUser(
                request,
                new ApiCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse loginResponse) {
                        Long userId = loginResponse.getUserId();
                        Log.d(TAG, "Login successful! User ID: " + userId);
                        // Save user session
                        prefsManager.saveUserSession(userId, loginResponse.getAccessToken());
                        // Navigate to home screen
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Login failed: " + errorMessage);
                        // Show error message
                    }
                });
    }

    /** Example: Create a new activity */
    public void exampleCreateActivity() {
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in!");
            return;
        }

        ActivityCreateRequest request =
                new ActivityCreateRequest(
                        "Weekend Hiking Trip",
                        "Let's explore the mountain trails this weekend!",
                        "2025-11-01T08:00:00",
                        "Mountain View Park",
                        10,
                        2,
                        "Sports");

        activityRepository.createActivity(
                currentUserId,
                request,
                new ApiCallback<Activity>() {
                    @Override
                    public void onSuccess(Activity activity) {
                        Log.d(TAG, "Activity created! ID: " + activity.getId());
                        // Show success message and navigate to activity details
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to create activity: " + errorMessage);
                        // Show error message
                    }
                });
    }

    /** Example: Get all trending activities */
    public void exampleGetTrendingActivities() {
        activityRepository.getTrendingActivities(
                new ApiCallback<List<Activity>>() {
                    @Override
                    public void onSuccess(List<Activity> activities) {
                        Log.d(TAG, "Fetched " + activities.size() + " trending activities");
                        // Update UI with activities list
                        for (Activity activity : activities) {
                            Log.d(TAG, "Activity: " + activity.getTitle());
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to fetch activities: " + errorMessage);
                        // Show error message
                    }
                });
    }

    /** Example: Express interest in an activity */
    public void exampleExpressInterest(Long activityId, boolean isFriend) {
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in!");
            return;
        }

        ExpressInterestRequest request = new ExpressInterestRequest(isFriend);

        participantRepository.expressInterest(
                activityId,
                currentUserId,
                request,
                new ApiCallback<Participant>() {
                    @Override
                    public void onSuccess(Participant participant) {
                        Log.d(TAG, "Interest expressed successfully!");
                        // Update UI to show "Interested" status
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to express interest: " + errorMessage);
                        // Show error message
                    }
                });
    }

    /** Example: Get participants for an activity */
    public void exampleGetParticipants(Long activityId) {
        participantRepository.getActivityParticipants(
                activityId,
                new ApiCallback<List<Participant>>() {
                    @Override
                    public void onSuccess(List<Participant> participants) {
                        Log.d(TAG, "Activity has " + participants.size() + " participants");
                        // Update UI with participants list
                        for (Participant participant : participants) {
                            Log.d(TAG, "Participant: " + participant.getUserName());
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to fetch participants: " + errorMessage);
                    }
                });
    }

    /** Example: Create a review */
    public void exampleCreateReview(Long activityId, Long reviewedUserId) {
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in!");
            return;
        }

        ReviewRequest request =
                new ReviewRequest(
                        reviewedUserId, activityId, 5, "Great person to hang out with!");

        reviewRepository.createReview(
                currentUserId,
                request,
                new ApiCallback<Review>() {
                    @Override
                    public void onSuccess(Review review) {
                        Log.d(TAG, "Review created successfully!");
                        // Show success message
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to create review: " + errorMessage);
                        // Show error message
                    }
                });
    }

    /** Example: Search activities by keyword */
    public void exampleSearchActivities(String keyword) {
        activityRepository.searchActivities(
                keyword,
                new ApiCallback<List<Activity>>() {
                    @Override
                    public void onSuccess(List<Activity> activities) {
                        Log.d(TAG, "Search returned " + activities.size() + " results");
                        // Update UI with search results
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Search failed: " + errorMessage);
                    }
                });
    }

    /** Example: Get user's own activities */
    public void exampleGetMyActivities() {
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in!");
            return;
        }

        activityRepository.getMyActivities(
                currentUserId,
                new ApiCallback<List<Activity>>() {
                    @Override
                    public void onSuccess(List<Activity> activities) {
                        Log.d(TAG, "You have created " + activities.size() + " activities");
                        // Update UI with user's activities
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to fetch activities: " + errorMessage);
                    }
                });
    }

    /** Example: Leave an activity */
    public void exampleLeaveActivity(Long activityId) {
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in!");
            return;
        }

        participantRepository.leaveActivity(
                activityId,
                currentUserId,
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully left the activity");
                        // Update UI to reflect leaving
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to leave activity: " + errorMessage);
                    }
                });
    }

    /** Example: Logout */
    public void exampleLogout() {
        prefsManager.clearUserSession();
        Log.d(TAG, "User logged out");
        // Navigate to login screen
    }

    /** Example: Check if user is logged in */
    public void exampleCheckLoginStatus() {
        if (prefsManager.isLoggedIn()) {
            Long userId = prefsManager.getUserId();
            Log.d(TAG, "User is logged in with ID: " + userId);
            // Navigate to home screen
        } else {
            Log.d(TAG, "User is not logged in");
            // Navigate to login screen
        }
    }
}
