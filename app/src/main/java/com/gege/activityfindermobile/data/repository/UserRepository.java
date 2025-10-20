package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.UserApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.LoginRequest;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.dto.UserRegistrationRequest;
import com.gege.activityfindermobile.data.model.User;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserApiService userApiService;

    @Inject
    public UserRepository(UserApiService userApiService) {
        this.userApiService = userApiService;
    }

    /**
     * Register a new user
     */
    public void registerUser(UserRegistrationRequest request, ApiCallback<User> callback) {
        userApiService.registerUser(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "User registered successfully: " + response.body().getEmail());
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Registration failed: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Login user
     */
    public void loginUser(LoginRequest request, ApiCallback<User> callback) {
        userApiService.loginUser(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "User logged in successfully: " + response.body().getEmail());
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Login failed: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Get user by ID
     */
    public void getUserById(Long userId, ApiCallback<User> callback) {
        userApiService.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "User fetched successfully: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to fetch user: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Get user by email
     */
    public void getUserByEmail(String email, ApiCallback<User> callback) {
        userApiService.getUserByEmail(email).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "User fetched by email successfully");
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to fetch user by email: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Update user profile
     */
    public void updateUserProfile(Long userId, Long authenticatedUserId,
                                   UserProfileUpdateRequest request, ApiCallback<User> callback) {
        userApiService.updateUserProfile(userId, authenticatedUserId, request)
                .enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "User profile updated successfully");
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to update profile: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Get all active users
     */
    public void getAllActiveUsers(ApiCallback<List<User>> callback) {
        userApiService.getAllActiveUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Fetched " + response.body().size() + " active users");
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to fetch active users: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Search users by name
     */
    public void searchUsers(String name, ApiCallback<List<User>> callback) {
        userApiService.searchUsers(name).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Search returned " + response.body().size() + " users");
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to search users: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Get users by interest
     */
    public void getUsersByInterest(String interest, ApiCallback<List<User>> callback) {
        userApiService.getUsersByInterest(interest).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Fetched users with interest: " + interest);
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to fetch users by interest: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Get top rated users
     */
    public void getTopRatedUsers(ApiCallback<List<User>> callback) {
        userApiService.getTopRatedUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Fetched top rated users");
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to fetch top rated users: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Deactivate user account
     */
    public void deactivateUser(Long userId, ApiCallbackVoid callback) {
        userApiService.deactivateUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "User deactivated successfully");
                    callback.onSuccess();
                } else {
                    String errorMsg = "Failed to deactivate user: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }
}
