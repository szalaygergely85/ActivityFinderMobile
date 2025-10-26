package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.UserPhotoApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.model.ImageUploadResponse;
import com.gege.activityfindermobile.data.model.UserPhoto;

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
public class UserPhotoRepository {
    private static final String TAG = "UserPhotoRepository";
    private final UserPhotoApiService userPhotoApiService;

    @Inject
    public UserPhotoRepository(UserPhotoApiService userPhotoApiService) {
        this.userPhotoApiService = userPhotoApiService;
    }

    /** Upload a new photo */
    public void uploadPhoto(File imageFile, ApiCallback<ImageUploadResponse> callback) {
        if (imageFile == null || !imageFile.exists()) {
            callback.onError("Image file not found");
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        userPhotoApiService
                .uploadPhoto(body)
                .enqueue(
                        new Callback<ImageUploadResponse>() {
                            @Override
                            public void onResponse(
                                    Call<ImageUploadResponse> call,
                                    Response<ImageUploadResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    ImageUploadResponse uploadResponse = response.body();
                                    Log.d(
                                            TAG,
                                            "Photo uploaded successfully: "
                                                    + uploadResponse.getUrl());
                                    callback.onSuccess(uploadResponse);
                                } else {
                                    String errorMsg = "Failed to upload photo: " + response.code();
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

    /** Get current user's photos */
    public void getMyPhotos(ApiCallback<List<UserPhoto>> callback) {
        userPhotoApiService
                .getMyPhotos()
                .enqueue(
                        new Callback<List<UserPhoto>>() {
                            @Override
                            public void onResponse(
                                    Call<List<UserPhoto>> call,
                                    Response<List<UserPhoto>> response) {
                                if (response.isSuccessful()) {
                                    List<UserPhoto> photos = response.body();
                                    if (photos != null) {
                                        Log.d(TAG, "Fetched " + photos.size() + " photos");
                                        callback.onSuccess(photos);
                                    } else {
                                        // Empty response body - treat as no photos
                                        Log.d(TAG, "No photos found");
                                        callback.onSuccess(new java.util.ArrayList<>());
                                    }
                                } else {
                                    String errorMsg = "Failed to fetch photos: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<UserPhoto>> call, Throwable t) {
                                // If deserialization fails, it might be an empty response
                                // Treat as empty list rather than error
                                Log.d(
                                        TAG,
                                        "No photos available (empty or invalid response): "
                                                + t.getMessage());
                                callback.onSuccess(new java.util.ArrayList<>());
                            }
                        });
    }

    /** Get another user's photos */
    public void getUserPhotos(Long userId, ApiCallback<List<UserPhoto>> callback) {
        userPhotoApiService
                .getUserPhotos(userId)
                .enqueue(
                        new Callback<List<UserPhoto>>() {
                            @Override
                            public void onResponse(
                                    Call<List<UserPhoto>> call,
                                    Response<List<UserPhoto>> response) {
                                if (response.isSuccessful()) {
                                    List<UserPhoto> photos = response.body();
                                    if (photos != null) {
                                        Log.d(TAG, "Fetched photos for user " + userId);
                                        callback.onSuccess(photos);
                                    } else {
                                        // Empty response body - treat as no photos
                                        Log.d(TAG, "No photos found for user " + userId);
                                        callback.onSuccess(new java.util.ArrayList<>());
                                    }
                                } else {
                                    String errorMsg =
                                            "Failed to fetch user photos: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<UserPhoto>> call, Throwable t) {
                                // If deserialization fails, it might be an empty response
                                // Treat as empty list rather than error
                                Log.d(
                                        TAG,
                                        "No photos available for user "
                                                + userId
                                                + " (empty or invalid response): "
                                                + t.getMessage());
                                callback.onSuccess(new java.util.ArrayList<>());
                            }
                        });
    }

    /** Set a photo as profile picture */
    public void setPhotoAsProfile(Long photoId, ApiCallback<UserPhoto> callback) {
        userPhotoApiService
                .setPhotoAsProfile(photoId)
                .enqueue(
                        new Callback<UserPhoto>() {
                            @Override
                            public void onResponse(
                                    Call<UserPhoto> call, Response<UserPhoto> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Photo set as profile picture");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to set profile picture: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<UserPhoto> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Delete a photo */
    public void deletePhoto(Long photoId, ApiCallbackVoid callback) {
        userPhotoApiService
                .deletePhoto(photoId)
                .enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Photo deleted successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg = "Failed to delete photo: " + response.code();
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

    /** Reorder photos */
    public void reorderPhotos(List<Long> photoIds, ApiCallbackVoid callback) {
        UserPhotoApiService.PhotoReorderRequest request =
                new UserPhotoApiService.PhotoReorderRequest(photoIds);

        userPhotoApiService
                .reorderPhotos(request)
                .enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Photos reordered successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to reorder photos: " + response.code();
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
