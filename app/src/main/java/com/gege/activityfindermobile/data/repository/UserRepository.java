package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.UserApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.LoginRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.dto.UserRegistrationRequest;
import com.gege.activityfindermobile.data.model.ImageUploadResponse;
import com.gege.activityfindermobile.data.model.User;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    /** Register a new user Returns LoginResponse containing JWT token and user data */
    public void registerUser(UserRegistrationRequest request, ApiCallback<LoginResponse> callback) {
        userApiService
                .registerUser(request)
                .enqueue(
                        new Callback<LoginResponse>() {
                            @Override
                            public void onResponse(
                                    Call<LoginResponse> call, Response<LoginResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    LoginResponse loginResponse = response.body();

                                    // Enhanced logging for debugging
                                    Log.d(TAG, "Registration response received");
                                    Log.d(
                                            TAG,
                                            "Access token present: "
                                                    + (loginResponse.getAccessToken() != null));
                                    Log.d(
                                            TAG,
                                            "User ID present: "
                                                    + (loginResponse.getUserId() != null));

                                    if (loginResponse.getUserId() != null) {
                                        Log.d(
                                                TAG,
                                                "User registered successfully: "
                                                        + loginResponse.getEmail());
                                    } else {
                                        Log.w(
                                                TAG,
                                                "Registration response does not contain user data");
                                    }

                                    callback.onSuccess(loginResponse);
                                } else {
                                    String errorMsg = "Registration failed: " + response.code();
                                    if (response.errorBody() != null) {
                                        try {
                                            errorMsg += " - " + response.errorBody().string();
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error reading error body", e);
                                        }
                                    }
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<LoginResponse> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Login user Returns LoginResponse containing JWT token and user data */
    public void loginUser(LoginRequest request, ApiCallback<LoginResponse> callback) {
        userApiService
                .loginUser(request)
                .enqueue(
                        new Callback<LoginResponse>() {
                            @Override
                            public void onResponse(
                                    Call<LoginResponse> call, Response<LoginResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    LoginResponse loginResponse = response.body();
                                    if (loginResponse.getUserId() != null) {
                                        Log.d(
                                                TAG,
                                                "User logged in successfully: "
                                                        + loginResponse.getEmail());
                                    }
                                    callback.onSuccess(loginResponse);
                                } else {
                                    String errorMsg = "Login failed: " + response.code();
                                    if (response.code() == 401) {
                                        errorMsg = "Invalid email or password";
                                    }
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<LoginResponse> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get user by ID */
    public void getUserById(Long userId, ApiCallback<User> callback) {
        userApiService
                .getUserById(userId)
                .enqueue(
                        new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "User fetched successfully: "
                                                    + response.body().getId());
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

    /** Get user by email */
    public void getUserByEmail(String email, ApiCallback<User> callback) {
        userApiService
                .getUserByEmail(email)
                .enqueue(
                        new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "User fetched by email successfully");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch user by email: " + response.code();
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

    /** Update user profile */
    public void updateUserProfile(
            Long userId,
            Long authenticatedUserId,
            UserProfileUpdateRequest request,
            ApiCallback<User> callback) {
        userApiService
                .updateUserProfile(userId, authenticatedUserId, request)
                .enqueue(
                        new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "User profile updated successfully");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to update profile: " + response.code();
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

    /** Get all active users */
    public void getAllActiveUsers(ApiCallback<List<User>> callback) {
        userApiService
                .getAllActiveUsers()
                .enqueue(
                        new Callback<List<User>>() {
                            @Override
                            public void onResponse(
                                    Call<List<User>> call, Response<List<User>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched " + response.body().size() + " active users");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch active users: " + response.code();
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

    /** Search users by name */
    public void searchUsers(String name, ApiCallback<List<User>> callback) {
        userApiService
                .searchUsers(name)
                .enqueue(
                        new Callback<List<User>>() {
                            @Override
                            public void onResponse(
                                    Call<List<User>> call, Response<List<User>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Search returned " + response.body().size() + " users");
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

    /** Get users by interest */
    public void getUsersByInterest(String interest, ApiCallback<List<User>> callback) {
        userApiService
                .getUsersByInterest(interest)
                .enqueue(
                        new Callback<List<User>>() {
                            @Override
                            public void onResponse(
                                    Call<List<User>> call, Response<List<User>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched users with interest: " + interest);
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch users by interest: " + response.code();
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

    /** Get top rated users */
    public void getTopRatedUsers(ApiCallback<List<User>> callback) {
        userApiService
                .getTopRatedUsers()
                .enqueue(
                        new Callback<List<User>>() {
                            @Override
                            public void onResponse(
                                    Call<List<User>> call, Response<List<User>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched top rated users");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch top rated users: " + response.code();
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

    /** Deactivate user account */
    public void deactivateUser(Long userId, ApiCallbackVoid callback) {
        userApiService
                .deactivateUser(userId)
                .enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "User deactivated successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to deactivate user: " + response.code();
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

    /**
     * Upload profile image for a user
     *
     * @param userId User ID to upload image for
     * @param imageFile The image file to upload
     * @param callback Callback with the image URL from server response
     */
    public void uploadProfileImage(Long userId, File imageFile, ApiCallback<String> callback) {
        if (imageFile == null || !imageFile.exists()) {
            callback.onError("Image file not found");
            return;
        }

        // Create RequestBody from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);

        // Create MultipartBody.Part (backend expects part named "file")
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        userApiService
                .uploadProfileImage(userId, body)
                .enqueue(
                        new Callback<ImageUploadResponse>() {
                            @Override
                            public void onResponse(
                                    Call<ImageUploadResponse> call,
                                    Response<ImageUploadResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    String imageUrl = response.body().getUrl();
                                    Log.d(TAG, "Profile image uploaded successfully: " + imageUrl);
                                    callback.onSuccess(imageUrl);
                                } else {
                                    String errorMsg =
                                            "Failed to upload profile image: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }
}
